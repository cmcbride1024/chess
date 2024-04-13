package server.webSocket;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataAccess.DataAccessException;
import dataAccess.InvalidGameID;
import dataAccess.MySqlDataAccess;
import dataAccess.UnauthorizedException;
import exception.ResponseException;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.UserService;
import webSocketMessages.serverMessages.*;
import webSocketMessages.serverMessages.Error;
import webSocketMessages.userCommands.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    private final ConcurrentHashMap<String, ConnectionManager> gameConnectionManagers = new ConcurrentHashMap<>();
    private final UserService service = new UserService(new MySqlDataAccess());

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, InvalidMoveException, ResponseException, SQLException, UnauthorizedException, DataAccessException, InvalidGameID {
        UserGameCommand userCommand = new Gson().fromJson(message, UserGameCommand.class);
        var commandType = userCommand.getCommandType();
        switch (commandType) {
            case JOIN_PLAYER ->  {
                JoinPlayer joinPlayer = new Gson().fromJson(message, JoinPlayer.class);
                handleJoinPlayer(session, joinPlayer);
            }
            case JOIN_OBSERVER -> {
                JoinObserver joinObserver = new Gson().fromJson(message, JoinObserver.class);
                handleJoinObserver(session, joinObserver);
            }
            case MAKE_MOVE -> {
                MakeMove makeMove = new Gson().fromJson(message, MakeMove.class);
                handleMakeMove(makeMove);
            }
            case LEAVE -> {
                Leave leave = new Gson().fromJson(message, Leave.class);
                handleLeave(leave);
            }
            case RESIGN -> {
                Resign resign = new Gson().fromJson(message, Resign.class);
                handleResign(resign);
            }
        }
    }

    private void handleJoinPlayer(Session session, JoinPlayer player) throws IOException, ResponseException, SQLException, UnauthorizedException, DataAccessException, InvalidGameID {
        var username = player.getUsername();
        String gameID = Integer.toString(player.getGameID());
        ConnectionManager connectionManager = gameConnectionManagers.computeIfAbsent(gameID, k -> new ConnectionManager());
        connectionManager.add(username, session);

        ChessGame game = null;
        var games = service.listGames(player.getAuthString());
        for (var listGame : games) {
            if (listGame.getGameID() == player.getGameID()) {
                game = listGame.getGame();
                break;
            }
        }

        service.joinGame(player.getAuthString(), player.getPlayerColor().toString(), player.getGameID());

        var message = String.format("%s has joined the game as %s", username, player.getPlayerColor().toString());
        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connectionManager.broadcast(username, notification);

        var loadGame = new LoadGame(ServerMessage.ServerMessageType.LOAD_GAME, game);
        connectionManager.sendMessage(username, loadGame);
    }

    private void handleJoinObserver(Session session, JoinObserver observer) throws IOException, ResponseException, UnauthorizedException, DataAccessException {
        var username = observer.getUsername();
        String gameID = Integer.toString(observer.getGameID());
        ConnectionManager connectionManager = gameConnectionManagers.computeIfAbsent(gameID, k -> new ConnectionManager());
        connectionManager.add(username, session);

        var message = String.format("%s is now observing game %s", username, gameID);
        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connectionManager.broadcast(username, notification);

        ChessGame game = null;
        var games = service.listGames(observer.getAuthString());
        for (var listGame : games) {
            if (listGame.getGameID() == observer.getGameID()) {
                game = listGame.getGame();
            }
        }

        var loadGame = new LoadGame(ServerMessage.ServerMessageType.LOAD_GAME, game);
        connectionManager.sendMessage(username, loadGame);
    }

    private void handleMakeMove(MakeMove makeMove) throws IOException, ResponseException, UnauthorizedException, DataAccessException, InvalidMoveException, SQLException {
        var username = makeMove.getUsername();
        String gameID = Integer.toString(makeMove.getGameID());
        ConnectionManager connectionManager = gameConnectionManagers.get(gameID);

        ChessGame chessGame = null;
        var games = service.listGames(makeMove.getAuthString());
        for (var listGame : games) {
            if (listGame.getGameID() == makeMove.getGameID()) {
                chessGame = listGame.getGame();
            }
        }

        assert chessGame != null;
        if (chessGame.getGameIsOver()) {
            throw new ResponseException(500, "Game is over");
        }

        var move = makeMove.getMove();
        var validMoves = chessGame.validMoves(move.getStartPosition());
        if (!validMoves.contains(move)) {
            var error = new Error(ServerMessage.ServerMessageType.ERROR, "Illegal move");
            connectionManager.sendMessage(username, error);
        }

        chessGame.makeMove(move);
        service.updateGame(makeMove.getAuthString(), chessGame, makeMove.getGameID());

        var message = String.format("%s moved from %s to %s", username, move.getStartPosition().toString(), move.getEndPosition().toString());
        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connectionManager.broadcast(username, notification);

        var loadGame = new LoadGame(ServerMessage.ServerMessageType.LOAD_GAME, chessGame);
        connectionManager.broadcast("", loadGame);
    }

    private void handleLeave(Leave leave) throws IOException, ResponseException, SQLException, UnauthorizedException, DataAccessException {
        var username = leave.getUsername();
        String gameID = Integer.toString(leave.getGameID());
        ConnectionManager connectionManager = gameConnectionManagers.get(gameID);
        var message = String.format("%s has left game %s", username, gameID);

        GameData gameData = service.getGameData(leave.getAuthString(), leave.getGameID());
        if (gameData.getWhiteUsername().equals(username)) {
            gameData = gameData.changeWhiteName(null);
            service.updateGameData(leave.getAuthString(), gameData);
        } else if (gameData.getBlackUsername().equals(username)) {
            gameData = gameData.changeBlackName(null);
            service.updateGameData(leave.getAuthString(), gameData);
        } else {
            message += " as an observer";
        }

        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connectionManager.broadcast(username, notification);

        connectionManager.remove(username);
    }

    private void handleResign(Resign resign) throws IOException, ResponseException, UnauthorizedException, DataAccessException, SQLException {
        var username = resign.getUsername();
        String gameID = Integer.toString(resign.getGameID());
        ConnectionManager connectionManager = gameConnectionManagers.get(gameID);

        ChessGame chessGame = null;
        var games = service.listGames(resign.getAuthString());
        for (var listGame : games) {
            if (listGame.getGameID() == resign.getGameID()) {
                chessGame = listGame.getGame();
            }
        }

        assert chessGame != null;
        chessGame.gameIsOver();

        service.updateGame(resign.getAuthString(), chessGame, resign.getGameID());

        var message = String.format("%s has resigned. The game is over", username);
        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connectionManager.broadcast("", notification);
    }
}
