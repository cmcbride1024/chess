package dataAccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.HashMap;
import java.util.Collection;
import java.util.List;

public interface DataAccess {
    UserData createUser(UserData u) throws DataAccessException;

    AuthData createAuth(UserData u) throws DataAccessException;

    Integer createGameId(String gameName) throws  DataAccessException;

    Integer createGame(GameData g) throws  DataAccessException;

    UserData getUser(String username) throws DataAccessException;

    HashMap<Integer, UserData> getUsers() throws DataAccessException;

    AuthData getAuth(String authToken) throws DataAccessException;

    HashMap<UserData, List<AuthData>> getAuths() throws DataAccessException;

    Collection<GameData> getGames() throws DataAccessException;

    GameData getGame(String gameName);

    void deleteAuth(AuthData auth) throws DataAccessException;

    void joinGame(String username, String playerColor, int gameID) throws DataAccessException, InvalidGameID;

    void clearUsers() throws DataAccessException;

    void clearGames() throws DataAccessException;

    void clearAuthTokens() throws DataAccessException;
}