package webSocketMessages.userCommands;

import model.AuthData;

public class Leave extends UserGameCommand {
    private final int gameID;
    private final String username;

    public Leave(AuthData authData, int gameID) {
        super(authData.authToken());
        this.gameID = gameID;
        this.username = authData.username();
    }

    public int getGameID() {
        return gameID;
    }

    public String getUsername() {
        return username;
    }
}
