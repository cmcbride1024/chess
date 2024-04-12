package webSocketMessages.userCommands;

import chess.ChessMove;
import model.AuthData;

public class MakeMove extends UserGameCommand {
    private final int gameID;
    private final ChessMove move;
    private final String username;

    public MakeMove(AuthData authData, int gameID, ChessMove move) {
        super(authData.authToken());
        this.gameID = gameID;
        this.move = move;
        this.username = authData.username();
    }

    public String getUsername() {
        return username;
    }

    public int getGameID() {
        return gameID;
    }

    public ChessMove getMove() {
        return move;
    }
}
