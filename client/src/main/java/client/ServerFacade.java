package client;

import model.AuthData;

public class ServerFacade {
    private final int port;
    public ServerFacade(int port) {
        this.port = port;
    }

    public AuthData register(String username, String password, String email) {
        return null;
    }
}
