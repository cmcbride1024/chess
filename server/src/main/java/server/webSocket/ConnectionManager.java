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
        String messageJson = gson.toJson(serverMessage, ServerMessage.class);
        var removeList = new ArrayList<Connection>();
        for (var connection : connections.values()) {
            if (connection.session.isOpen()) {
                if (!connection.visitorName.equals(excludeVisitorName)) {
                    connection.send(messageJson);
                }
            } else {
                removeList.add(connection);
            }
        }

        for (var connection : removeList) {
            connections.remove(connection.visitorName);
        }
    }

    public void sendMessage(String username, ServerMessage serverMessage) throws IOException {
        Connection connection = connections.get(username);
        if (connection != null && connection.session.isOpen()) {
            switch (serverMessage.getServerMessageType()) {
                case LOAD_GAME -> connection.send(new Gson().toJson(serverMessage, LoadGame.class));
                case ERROR -> connection.send(new Gson().toJson(serverMessage, Error.class));
                case NOTIFICATION -> connection.send(new Gson().toJson(serverMessage, Notification.class));
            }
        }
    }
}
