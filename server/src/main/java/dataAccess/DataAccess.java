package dataAccess;

import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;

import javax.xml.crypto.Data;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

public interface DataAccess {
    void createUser(UserData u) throws DataAccessException;

    AuthData createAuth(UserData u) throws DataAccessException;

    Integer createGameId(String gameName) throws  DataAccessException;

    void createGame(GameData g) throws  DataAccessException;

    UserData getUser(String username) throws DataAccessException;

    AuthData getAuth(String authToken) throws DataAccessException;

    HashMap<UserData, List<AuthData>> getAuths() throws DataAccessException;

    Collection<GameData> getGames() throws DataAccessException;

    void deleteAuth(AuthData auth) throws DataAccessException;

    void joinGame(String username, String playerColor, int gameID) throws DataAccessException, InvalidGameID;

    void clearUsers() throws DataAccessException, ResponseException, SQLException;

    void clearGames() throws DataAccessException, ResponseException, SQLException;

    void clearAuthTokens() throws DataAccessException, ResponseException, SQLException;

    void clearGameIds() throws DataAccessException, ResponseException, SQLException;
}