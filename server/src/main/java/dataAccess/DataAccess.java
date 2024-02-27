package dataAccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.HashSet;
import java.util.Collection;

public interface DataAccess {
    UserData createUser(UserData u) throws DataAccessException;

    AuthData createAuth(UserData u) throws DataAccessException;

    Integer createGameId(String gameName) throws  DataAccessException;

    Integer createGame(GameData g) throws  DataAccessException;

    UserData getUser(String username) throws DataAccessException;

    AuthData getAuth(String authToken) throws DataAccessException;

    Collection<GameData> getGames() throws DataAccessException;

    GameData getGame(String gameName) throws DataAccessException;

    void deleteAuth(String username) throws DataAccessException;

    void joinGame(String username, String playerColor, int gameID) throws DataAccessException;

    void clearUsers() throws DataAccessException;

    void clearGames() throws DataAccessException;

    void clearAuthTokens() throws DataAccessException;
}