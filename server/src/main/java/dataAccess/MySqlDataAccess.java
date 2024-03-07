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
    public void clearUsers() throws DataAccessException {

    }

    @Override
    public void clearGames() throws DataAccessException {

    }

    @Override
    public void clearAuthTokens() throws DataAccessException {
        String statement = "TRUNCATE authTokens";
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
