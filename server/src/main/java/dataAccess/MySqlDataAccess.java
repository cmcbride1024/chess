package dataAccess;

import com.google.gson.Gson;
import model.AuthData;
import model.UserData;
import model.GameData;

import java.util.*;
import java.sql.*;

import exception.ResponseException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class MySqlDataAccess implements DataAccess {

    public MySqlDataAccess() throws ResponseException, DataAccessException {
        configureDatabase();
    }

    private String hashPassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException, ResponseException, SQLException {
        String statement = "INSERT INTO users (username, userData) VALUES (?, ?)";
        String hashedPassword = hashPassword(userData.password());
        UserData newUserData = new UserData(userData.username(), hashedPassword, userData.email());
        var jsonUser = new Gson().toJson(newUserData);
        executeUpdate(statement, newUserData.username(), jsonUser);
    }

    @Override
    public AuthData createAuth(UserData userData) throws DataAccessException, ResponseException, SQLException {
        String statement = "INSERT INTO authTokens (userData, authData, authToken) VALUES (?, ?, ?)";
        String newUUID = UUID.randomUUID().toString();
        AuthData authData = new AuthData(newUUID, userData.username());
        UserData newUserData = new UserData(userData.username(), hashPassword(userData.password()), userData.email());

        var jsonUser = new Gson().toJson(newUserData);
        var jsonAuth = new Gson().toJson(authData);
        executeUpdate(statement, jsonUser, jsonAuth, newUUID);

        return authData;
    }

    @Override
    public Integer createGameID(String gameName) throws DataAccessException, ResponseException, SQLException {
        String statement = "INSERT INTO gameIDs (gameName) VALUES (?)";
        return executeUpdate(statement, gameName);
    }

    @Override
    public void createGame(GameData gameData) throws DataAccessException, ResponseException, SQLException {
        String statement = "INSERT INTO games (gameData) VALUES (?)";
        var jsonGame = new Gson().toJson(gameData);
        executeUpdate(statement, jsonGame);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException, ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            String statement = "SELECT userData FROM users WHERE username=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String userJson = rs.getString("userData");
                        return new Gson().fromJson(userJson, UserData.class);
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException, ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            String statement = "SELECT authData FROM authTokens WHERE authToken=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String authJson = rs.getString("authData");
                        return new Gson().fromJson(authJson, AuthData.class);
                    }
                }
            }
        } catch (SQLException e) {
            throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
        }

        return null;
    }

    @Override
    public HashMap<UserData, List<AuthData>> getAuths() throws DataAccessException, ResponseException {
        var result = new HashMap<UserData, List<AuthData>>();
        try (var conn = DatabaseManager.getConnection()) {
            String statement = "SELECT userData, authData FROM authTokens";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String userJson = rs.getString("userData");
                        String authJson = rs.getString("authData");
                        UserData userData = new Gson().fromJson(userJson, UserData.class);
                        AuthData authData = new Gson().fromJson(authJson, AuthData.class);

                        if (result.get(userData) == null) {
                            result.put(userData, new ArrayList<AuthData>(Collections.singleton(authData)));
                        } else {
                            result.get(userData).add(authData);
                        }
                        result.computeIfAbsent(userData, key -> Collections.singletonList(authData));
                    }
                }
            }
        } catch (SQLException e) {
            throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
        }

        return result;
    }

    @Override
    public Collection<GameData> getGames() throws DataAccessException, ResponseException {
        ArrayList<GameData> games = new ArrayList<>();
        try (var conn = DatabaseManager.getConnection()) {
            String statement = "SELECT gameData FROM games";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String gameJson = rs.getString("gameData");
                        GameData gameData = new Gson().fromJson(gameJson, GameData.class);
                        games.add(gameData);
                    }
                }
            }
        } catch (SQLException e) {
            throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
        }

        return games;
    }

    @Override
    public void deleteAuth(AuthData auth) throws DataAccessException, ResponseException, SQLException {
        String statement = "DELETE FROM authTokens WHERE authData=?";
        executeUpdate(statement, auth);
    }

    private void deleteGame(GameData gameData) throws DataAccessException, ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            String statement = "DELETE FROM games WHERE gameData=?";
            try (var ps = conn.prepareStatement(statement)) {
                String gameJson = new Gson().toJson(gameData);
                ps.setString(1, gameJson);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
    }

    @Override
    public void joinGame(String username, String playerColor, int gameID) throws DataAccessException, InvalidGameID, ResponseException, SQLException {
        for (GameData game : getGames()) {
            if (game.gameID() == gameID && playerColor != null) {
                switch(playerColor) {
                    case "WHITE":
                        if (game.whiteUsername() != null) {
                            throw new DataAccessException("Player has already joined as white.");
                        }
                        createGame(game.changeWhiteName(username));
                        break;
                    case "BLACK":
                        if (game.blackUsername() != null) {
                            throw new DataAccessException("Player has already joined as white.");
                        }
                        createGame(game.changeBlackName(username));
                        break;
                }
                deleteGame(game);
                return;
            }
        }

        throw new InvalidGameID("Game does not exist.");
    }

    @Override
    public void clearUsers() throws DataAccessException, ResponseException, SQLException {
        String statement = "TRUNCATE users";
        executeUpdate(statement);
    }

    @Override
    public void clearGames() throws DataAccessException, ResponseException, SQLException {
        String statement = "TRUNCATE games";
        executeUpdate(statement);
    }

    @Override
    public void clearAuthTokens() throws DataAccessException, ResponseException, SQLException {
        String statement = "TRUNCATE authTokens";
        executeUpdate(statement);
    }

    @Override
    public void clearGameIDs() throws DataAccessException, ResponseException, SQLException {
        String statement = "TRUNCATE gameIDs";
        executeUpdate(statement);
    }

    private int executeUpdate(String statement, Object... parameters) throws ResponseException, DataAccessException, SQLException {
        try (var conn  = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < parameters.length; i++) {
                    var parameter = parameters[i];
                    switch (parameter) {
                        case String p -> ps.setString(i + 1, p);
                        case Integer p -> ps.setInt(i + 1, p);
                        case AuthData p -> ps.setString(i + 1, p.toString());
                        case GameData p -> ps.setString(i + 1, p.toString());
                        case UserData p -> ps.setString(i + 1, p.toString());
                        case null -> ps.setNull(i + 1, NULL);
                        default -> {}
                    }
                }
                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new ResponseException(500, String.format("Unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS users (
                `userID` int NOT NULL AUTO_INCREMENT,
                `username` TEXT DEFAULT NULL,
                `userData` TEXT DEFAULT NULL,
                PRIMARY KEY (`userID`)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS authTokens (
                `userData` TEXT DEFAULT NULL,
                `authData` TEXT DEFAULT NULL,
                `authToken` TEXT DEFAULT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS gameIDs (
                `gameID` int NOT NULL AUTO_INCREMENT,
                `gameName` TEXT DEFAULT NULL,
                PRIMARY KEY (`gameID`)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS games (
                `gameData` TEXT DEFAULT NULL
            )
            """
    };

    private void configureDatabase() throws ResponseException, DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new ResponseException(500, String.format("Unable to configure database: %s", e.getMessage()));
        }
    }
}
