package webSocketMessages.userCommands;

import chess.ChessGame;
import model.AuthData;

public class JoinPlayer extends UserGameCommand {
    private final int gameID;
    private final ChessGame.TeamColor playerColor;
    private final String username;

    public JoinPlayer(AuthData authData, int gameID, ChessGame.TeamColor playerColor) {
        super(authData.authToken());
        this.username = authData.username();
        this.gameID = gameID;
        this.playerColor = playerColor;
    }

    public int getGameID() {
        return gameID;
    }

    public String getUsername() {
        return username;
    }

    public ChessGame.TeamColor getPlayerColor() {
        return playerColor;
    }
}
