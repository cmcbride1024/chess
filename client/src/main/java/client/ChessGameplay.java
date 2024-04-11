package client;

import chess.ChessGame;

import java.util.Arrays;
import java.util.Objects;

import static ui.EscapeSequences.*;

public class ChessGameplay {
    private ChessGame gameState;
    private final ChessGame.TeamColor playerColor;
    private final ChessClient client;

    public ChessGameplay(ChessClient client, ChessGame.TeamColor playerColor) {
        this.playerColor = playerColor;
        this.client = client;
    }

    public String eval(String... tokens) {
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);

        return switch(cmd) {
            case "redraw" -> redrawBoard();
            case "leave" -> leaveGame(params);
            case "move" -> makeMove(params);
            case "resign" -> resignGame(params);
            case "possible" -> legalMoves(params);
            default -> help();
        };
    }

    private String getLabel(int row, int col) {
        String[] pieces = {" ", "R", "N", "B", "Q", "K", "B", "N", "R", " "};
        String[] columnLabels = {" ", "a", "b", "c", "d", "e", "f", "g", "h", " "};
        String[] rowLabels = {" ", "8", "7", "6", "5", "4", "3", "2", "1", " "};
        String piece = " ";
        if (row == 0 || row == 9) {
            piece = columnLabels[col];
        } else if (col == 0 || col == 9) {
            piece = rowLabels[row];
        } else if (row == 1 || row == 8 || row == 2 || row == 7) {
            piece = pieces[col];
        }
        return piece;
    }

    private String chessCharacterLookup(int row, int col) {
        String piece = getLabel(row, col);

        if (row == 1 || row == 0 || row == 9 || col == 0 || col == 9) {
            // Black pieces and labels
            return piece.equals(" ") ? piece : SET_TEXT_COLOR_BLACK + piece + RESET_TEXT_COLOR;
        } else if (row == 8) {
            // White pieces
            return piece.equals(" ") ? piece : SET_TEXT_COLOR_WHITE + piece + RESET_TEXT_COLOR;
        }

        if (row == 2) {
            // Black pawns
            return SET_TEXT_COLOR_BLACK + "P" + RESET_TEXT_COLOR;
        } else if (row == 7) {
            // White pawns
            return SET_TEXT_COLOR_WHITE + "P" + RESET_TEXT_COLOR;
        }
        return " ";
    }

    public String boardLayout(String playerColor) {
        var boards = new StringBuilder();
        for (int row = 0; row <= 9; row++) {
            for (int col = 0; col <= 9; col++) {
                int newRow = (Objects.equals(playerColor, "white")) ? row : 9 - row;
                int newCol = (Objects.equals(playerColor, "white")) ? col : 9 - col;
                String chessCharacter = chessCharacterLookup(newRow, newCol);
                if (row == 0 || col == 0 || row == 9 || col == 9) {
                    boards.append(SET_BG_COLOR_LIGHT_GREY);
                } else {
                    boolean isDark = (row + col) % 2 == 1;
                    String bgColor = isDark ? SET_BG_COLOR + "95m" : SET_BG_COLOR + "222m";

                    boards.append(bgColor);
                }
                boards.append(String.format(" %s ", chessCharacter)).append("\u001B[49m");
            }
            boards.append('\n').append(SET_TEXT_COLOR_WHITE).append("\u001B[49m");
        }
        return boards.toString();
    }

    public String redrawBoard() {
        return null;
    }

    public String leaveGame(String... params) {
        client.gameOver();
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
        return "";
    }
}
