package service;

import chess.ChessGame;
import dataAccess.DataAccessException;
import dataAccess.MemoryDataAccess;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

public class UserService {
    private final MemoryDataAccess dataAccess;

    public UserService(MemoryDataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData user) throws DataAccessException {
        var existingUser = dataAccess.getUser(user.username());

        if (existingUser == null) {
            dataAccess.createUser(user);
            return dataAccess.createAuth(user);
        } else {
            throw new DataAccessException("Username already exists in the database.");
        }
    }

    public AuthData login(UserData user) throws DataAccessException {
        var existingUser = dataAccess.getUser(user.username());

        if (existingUser != null) {
            return dataAccess.createAuth(user);
        } else {
            throw new DataAccessException("Username already exists in the database.");
        }
    }

    public void logout(AuthData auth) throws DataAccessException {
        if (!dataAccess.getAuths().containsValue(auth)) {
            throw new DataAccessException("User is not already logged in.");
        }

        dataAccess.deleteAuth(auth);
    }

    public Collection<GameData> listGames(AuthData auth) throws DataAccessException {
        if (!dataAccess.getAuths().containsValue(auth)) {
            throw new DataAccessException("User is not registered with the system.");
        }

        return dataAccess.getGames();
    }

    public void createGame(AuthData authToken, String gameName) throws DataAccessException {
        if (!dataAccess.getAuths().containsValue(authToken)) {
            throw new DataAccessException("User is not registered with the system.");
        }

        int newGameID = dataAccess.createGameId(gameName);
        var game = new ChessGame();
        dataAccess.createGame(new GameData(newGameID, null, null, gameName, game));
    }

    public void joinGame(AuthData authToken, String playerColor, Integer gameID) throws DataAccessException {

    }

    public void clearApplication() throws DataAccessException {
        dataAccess.clearUsers();
        dataAccess.clearGames();
        dataAccess.clearAuthTokens();
    }
}
