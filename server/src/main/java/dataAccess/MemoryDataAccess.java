package dataAccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.HashSet;

public class MemoryDataAccess {
    void createUser(UserData u) throws DataAccessException {

    }

    void createAuth(UserData u) throws DataAccessException {

    }

    void createGameID(GameData g) throws  DataAccessException {

    }

    void createGame(GameData g) throws  DataAccessException {

    }

    UserData getUser(String username) throws DataAccessException  {
        return null;
    }

    AuthData getAuth(String authToken) throws DataAccessException  {
        return null;
    }

    HashSet<GameData> getGames() throws DataAccessException  {
        return new HashSet<>();
    }

    GameData getGame(String gameName) throws DataAccessException  {
        return null;
    }

    void deleteAuth(String username) throws DataAccessException {
        return;
    }

    void joinGame(String username, String playerColor, int gameID) throws DataAccessException {
        return;
    }

    void clearUsers() throws DataAccessException {
        return;
    }

    void clearGames() throws DataAccessException {
        return;
    }

    void clearAuthTokens() throws DataAccessException {
        return;
    }
}
