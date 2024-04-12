package service;

import chess.ChessBoard;
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
        } else if (!dataAccess.passwordsMatch(password, existingUser.password())) {
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
        ChessBoard newBoard = new ChessBoard();
        newBoard.resetBoard();
        game.setBoard(newBoard);
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

    public GameData getGameData(String authToken, Integer gameID) throws ResponseException, DataAccessException, UnauthorizedException, SQLException {
        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            throw new UnauthorizedException("User is not registered with the system.");
        }

        return dataAccess.getGameData(gameID);
    }

    public void updateGame(String authToken, ChessGame newGame, int gameID) throws ResponseException, DataAccessException, UnauthorizedException, SQLException {
        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            throw new UnauthorizedException("User is not registered with the system.");
        }

        GameData existingGame = dataAccess.getGameData(gameID);
        if (existingGame == null) {
            throw new DataAccessException("No game found with the provided game ID.");
        }

        if (!existingGame.getWhiteUsername().equals(authData.username()) && !existingGame.getBlackUsername().equals(authData.username())) {
            throw new UnauthorizedException("User is not authorized to update this game.");
        }

        existingGame.setGame(newGame);

        dataAccess.updateGame(existingGame);
    }

    public void updateGameData(String authToken, GameData newGameData) throws UnauthorizedException, ResponseException, DataAccessException {
        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            throw new UnauthorizedException("User is not registered with the system.");
        }

        dataAccess.updateGame(newGameData);
    }

    public void clearApplication() throws DataAccessException, ResponseException, SQLException {
        dataAccess.clearUsers();
        dataAccess.clearGames();
        dataAccess.clearAuthTokens();
        dataAccess.clearGameIDs();
    }
}
