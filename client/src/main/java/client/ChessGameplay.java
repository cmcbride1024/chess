package client;

import chess.ChessGame;

import java.util.Arrays;
import static ui.EscapeSequences.*;

public class ChessGameplay {
    ChessGame gameState;

    public ChessGameplay() {

    }

    public String eval(String... tokens) {
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);

        return switch(cmd) {
            case "redraw" -> redrawBoard(params);
            case "leave" -> leaveGame(params);
            case "move" -> makeMove(params);
            case "resign" -> resignGame(params);
            case "possible" -> legalMoves(params);
            default -> help();
        };
    }

    public String redrawBoard(String... params) {
        return null;
    }

    public String leaveGame(String... params) {
        return null;
    }

    public String makeMove(String... params) {
        return null;
    }

    public String resignGame(String... params) {
        return null;
    }

    public String legalMoves(String... params) {
        return null;
    }

    public String help() {
        return null;
    }
}
