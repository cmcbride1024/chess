package dataAccess;

import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

public interface DataAccess {
    void createUser(UserData userData) throws DataAccessException, ResponseException, SQLException;

    AuthData createAuth(UserData userData) throws DataAccessException, ResponseException, SQLException;

    Integer createGameID(String gameName) throws DataAccessException, ResponseException, SQLException;

    void createGame(GameData gameData) throws DataAccessException, ResponseException, SQLException;

    UserData getUser(String username) throws DataAccessException, ResponseException;

    AuthData getAuth(String authToken) throws DataAccessException, ResponseException;

    HashMap<UserData, List<AuthData>> getAuths() throws DataAccessException, ResponseException;

    Collection<GameData> getGames() throws DataAccessException;

    void deleteAuth(AuthData auth) throws DataAccessException, ResponseException, SQLException;

    void joinGame(String username, String playerColor, int gameID) throws DataAccessException, InvalidGameID;

    void clearUsers() throws DataAccessException, ResponseException, SQLException;

    void clearGames() throws DataAccessException, ResponseException, SQLException;

    void clearAuthTokens() throws DataAccessException, ResponseException, SQLException;

    void clearGameIDs() throws DataAccessException, ResponseException, SQLException;
}