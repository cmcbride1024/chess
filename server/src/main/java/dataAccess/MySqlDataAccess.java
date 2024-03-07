package dataAccess;

import model.AuthData;
import model.UserData;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import exception.ResponseException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class MySqlDataAccess implements DataAccess {

    public MySqlDataAccess() throws ResponseException, DataAccessException {
        configureDatabase();
    }

    public void createUser(UserData u) throws DataAccessException {

    }

    public AuthData createAuth(UserData u) throws DataAccessException {
        return null;
    }

    public Integer createGameId(String gameName) throws DataAccessException {
        return null;
    }

    @Override
    public void createGame(GameData g) throws DataAccessException {

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
    public void deleteAuth(AuthData auth) throws DataAccessException {

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

    public void clearGameIds() throws DataAccessException, ResponseException, SQLException {
        String statement = "TRUNCATE gameIds";
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
            CREATE TABLE IF NOT EXISTS gameIds (
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
