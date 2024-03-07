package dataAccess;

import com.google.gson.Gson;
import model.AuthData;
import model.UserData;
import model.GameData;

import java.util.*;
import java.sql.*;

import exception.ResponseException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class MySqlDataAccess implements DataAccess {

    public MySqlDataAccess() throws ResponseException, DataAccessException {
        configureDatabase();
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException, ResponseException, SQLException {
        String statement = "INSERT INTO users (userData) VALUES (?)";
        var jsonUser = new Gson().toJson(userData);
        executeUpdate(statement, jsonUser);
    }

    @Override
    public AuthData createAuth(UserData userData) throws DataAccessException, ResponseException, SQLException {
        String statement = "INSERT INTO authTokens (userData, authData) VALUES (?, ?)";
        String newUUID = UUID.randomUUID().toString();
        AuthData authData = new AuthData(newUUID, userData.username());

        var jsonUser = new Gson().toJson(userData);
        var jsonAuth = new Gson().toJson(authData);
        executeUpdate(statement, jsonUser, jsonAuth);

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
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public HashMap<UserData, List<AuthData>> getAuths() throws DataAccessException {
        return null;
    }

    @Override
    public Collection<GameData> getGames() throws DataAccessException {
        return null;
    }

    @Override
    public void deleteAuth(AuthData auth) throws DataAccessException, ResponseException, SQLException {
        String statement = "DELETE FROM authTokens WHERE authData=?";
        executeUpdate(statement, auth);
    }

    @Override
    public void joinGame(String username, String playerColor, int gameID) throws DataAccessException, InvalidGameID {

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
                        case AuthData p -> ps.setString(i+1, p.toString());
                        case GameData p -> ps.setString(i+1, p.toString());
                        case UserData p -> ps.setString(i+1, p.toString());
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
                `userData` TEXT DEFAULT NULL,
                PRIMARY KEY (`userID`)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS authTokens (
                `userData` TEXT DEFAULT NULL,
                `authData` TEXT DEFAULT NULL
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
