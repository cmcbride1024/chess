package server.webSocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import webSocketMessages.serverMessages.*;
import webSocketMessages.serverMessages.Error;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void add(String username, Session session) {
        var connection = new Connection(username, session);
        connections.put(username, connection);
    }

    public void remove(String username) {
        connections.remove(username);
    }

    public void broadcast(String excludeVisitorName, ServerMessage serverMessage) throws IOException {
        Gson gson = new Gson();
        var removeList = new ArrayList<Connection>();
        for (var connection : connections.values()) {
            if (connection.session.isOpen()) {
                if (!connection.visitorName.equals(excludeVisitorName)) {
                    switch (serverMessage.getServerMessageType()) {
                        case LOAD_GAME -> connection.send(gson.toJson(serverMessage, LoadGame.class));
                        case ERROR -> connection.send(gson.toJson(serverMessage, Error.class));
                        case NOTIFICATION -> connection.send(gson.toJson(serverMessage, Notification.class));
                    }
                }
            } else {
                removeList.add(connection);
            }
        }

        for (var connection : removeList) {
            connections.remove(connection.visitorName);
        }
    }

    public void sendMessage(String authToken, ServerMessage serverMessage) throws IOException {
        Gson gson = new Gson();
        Connection connection = connections.get(authToken);
        if (connection != null && connection.session.isOpen()) {
            switch (serverMessage.getServerMessageType()) {
                case LOAD_GAME -> connection.send(gson.toJson(serverMessage, LoadGame.class));
                case ERROR -> connection.send(gson.toJson(serverMessage, Error.class));
                case NOTIFICATION -> connection.send(gson.toJson(serverMessage, Notification.class));
            }
        }
    }
}
