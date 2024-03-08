package service;

import chess.ChessGame;
import dataAccess.*;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.util.Collection;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData user) throws UnauthorizedException, DataAccessException, ResponseException, SQLException {
        var existingUser = dataAccess.getUser(user.username());

        if (existingUser == null) {
            dataAccess.createUser(user);
            return dataAccess.createAuth(user);
        } else {
            throw new UnauthorizedException("Username already exists in the database.");
        }
    }

    public AuthData login(String username, String password) throws DataAccessException, UnauthorizedException, ResponseException, SQLException {
        var existingUser = dataAccess.getUser(username);

        if (existingUser == null) {
            throw new UnauthorizedException("Username has not been registered.");
        } else if (!existingUser.password().equals(password)) {
            throw new DataAccessException("Incorrect password.");
        } else {
            return dataAccess.createAuth(existingUser);
        }
    }

    public void logout(String authToken) throws UnauthorizedException, DataAccessException, ResponseException, SQLException {
        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            throw new UnauthorizedException("User is not registered with the system.");
        }

        dataAccess.deleteAuth(authData);
    }

    public Collection<GameData> listGames(String authToken) throws UnauthorizedException, DataAccessException, ResponseException {
        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            throw new UnauthorizedException("User is not registered with the system.");
        }

        return dataAccess.getGames();
    }

    public int createGame(String authToken, String gameName) throws UnauthorizedException, DataAccessException, ResponseException, SQLException {
        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            throw new UnauthorizedException("User is not registered with the system.");
        }

        int newGameID = dataAccess.createGameID(gameName);
        ChessGame game = new ChessGame();
        dataAccess.createGame(new GameData(newGameID, null, null, gameName, game));

        return newGameID;
    }

    public void joinGame(String authToken, String playerColor, Integer gameID) throws DataAccessException, UnauthorizedException, InvalidGameID, ResponseException, SQLException {
        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            throw new UnauthorizedException("User is not registered with the system.");
        }

        var username = dataAccess.getAuth(authToken).username();
        dataAccess.joinGame(username, playerColor, gameID);
    }

    public void clearApplication() throws DataAccessException, ResponseException, SQLException {
        dataAccess.clearUsers();
        dataAccess.clearGames();
        dataAccess.clearAuthTokens();
        dataAccess.clearGameIDs();
    }
}
