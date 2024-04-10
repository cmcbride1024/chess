package client.webSocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import exception.ResponseException;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.UserGameCommand;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {
    Session session;
    NotificationHandler notificationHandler;

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/connect");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                     ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                     notificationHandler.notify(serverMessage);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endPointConfig) {
    }

    public void joinGame(int gameID, ChessGame.TeamColor playerColor, String authToken) throws ResponseException {
        try {
            var joinCommand = new UserGameCommand(authToken);
            this.session.getBasicRemote().sendText(new Gson().toJson(joinCommand));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void observeGame(int gameID, String authToken) throws ResponseException {
        try {
            var observeCommand = new UserGameCommand(authToken);
            this.session.getBasicRemote().sendText(new Gson().toJson(observeCommand));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void makeMove(int gameID, ChessMove move, String authToken) throws ResponseException {
        try {
            var moveCommand = new UserGameCommand(authToken);
            this.session.getBasicRemote().sendText(new Gson().toJson(moveCommand));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void leaveGame(String authToken, int gameID) throws ResponseException {

    }

    public void resign(String authToken, int gameID) throws ResponseException {

    }
}
