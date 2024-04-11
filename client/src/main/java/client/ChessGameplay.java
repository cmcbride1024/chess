package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPosition;
import exception.ResponseException;

import java.util.Arrays;
import java.util.Objects;

import static chess.ChessPiece.PieceType.*;
import static ui.EscapeSequences.*;

public class ChessGameplay {
    private ChessGame gameState;
    private final ChessGame.TeamColor playerColor;
    private final ChessClient client;

    public ChessGameplay(ChessClient client, ChessGame.TeamColor playerColor) {
        this.playerColor = playerColor;
        this.client = client;
    }

    public String eval(String... tokens) throws ResponseException {
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);

        return switch(cmd) {
            case "redraw" -> redrawBoard();
            case "leave" -> leaveGame(params);
            case "move" -> makeMove(params);
            case "resign" -> resignGame(params);
            case "valid" -> legalMoves(params);
            default -> help();
        };
    }

    private String getLabel(int row, int col) {
        String[] columnLabels = {" ", "a", "b", "c", "d", "e", "f", "g", "h", " "};
        String[] rowLabels = {" ", "8", "7", "6", "5", "4", "3", "2", "1", " "};
        String piece = " ";
        if (row == 0 || row == 9) {
            piece = columnLabels[col];
        } else if (col == 0 || col == 9) {
            piece = rowLabels[row];
        } else {
            ChessBoard board = gameState.getBoard();
            var tempPiece = board.getPiece(new ChessPosition(row, col));
            if (tempPiece != null) {
                piece = switch (tempPiece.getPieceType()) {
                    case ROOK -> "R";
                    case KING -> "K";
                    case QUEEN -> "Q";
                    case BISHOP -> "B";
                    case KNIGHT -> "N";
                    case PAWN -> "P";
                };
            } else {
                piece = " ";
            }
        }
        return piece;
    }

    private String chessCharacterLookup(int row, int col) {
        String piece = getLabel(row, col);
        var teamColor = gameState.getBoard().getPiece(new ChessPosition(row, col)).getTeamColor();

        if (teamColor.equals(ChessGame.TeamColor.WHITE)) {
            return piece.equals(" ") ? piece : SET_TEXT_COLOR_WHITE + piece + RESET_TEXT_COLOR;
        }
        return piece.equals(" ") ? piece : SET_TEXT_COLOR_BLACK + piece + RESET_TEXT_COLOR;
    }

    public String boardLayout(ChessGame.TeamColor playerColor, boolean highlight, ChessPosition position) {
        var board = new StringBuilder();
        for (int row = 0; row <= 9; row++) {
            for (int col = 0; col <= 9; col++) {
                int newRow = (Objects.equals(playerColor, ChessGame.TeamColor.WHITE)) ? row : 9 - row;
                int newCol = (Objects.equals(playerColor, ChessGame.TeamColor.WHITE)) ? col : 9 - col;
                String chessCharacter = chessCharacterLookup(newRow, newCol);
                if (row == 0 || col == 0 || row == 9 || col == 9) {
                    board.append(SET_BG_COLOR_LIGHT_GREY);
                } else {
                    boolean isDark = (row + col) % 2 == 1;
                    String bgColor;

                    if (highlight) {
                        var validMoves = gameState.validMoves(position);
                        boolean containsSquare = false;
                        for (var move : validMoves) {
                            if (move.getEndPosition().equals(new ChessPosition(row, col))) {
                                containsSquare = true;
                                break;
                            }
                        }

                        if (containsSquare) {
                            bgColor = isDark ? SET_BG_COLOR + "32m" : SET_BG_COLOR + "92m";
                        } else {
                            bgColor = isDark ? SET_BG_COLOR + "95m" : SET_BG_COLOR + "222m";
                        }
                    } else {
                        bgColor = isDark ? SET_BG_COLOR + "95m" : SET_BG_COLOR + "222m";
                    }

                    board.append(bgColor);
                }
                board.append(String.format(" %s ", chessCharacter)).append("\u001B[49m");
            }
            board.append('\n').append(SET_TEXT_COLOR_WHITE).append("\u001B[49m");
        }
        return board.toString();
    }

    public String redrawBoard() {
        return boardLayout(playerColor, false, null);
    }

    public String leaveGame(String... params) {
        client.gameOver();
        return null;
    }

    private ChessPosition generatePosition(String position) throws ResponseException {
        try {
            int row = position.charAt(1);
            int col = Character.toLowerCase(position.charAt(0)) - 'a' + 1;
            return new ChessPosition(row, col);
        } catch (Exception ex) {
            throw new ResponseException(400, ex.getMessage());
        }
    }

    public String makeMove(String... params) {
        return null;
    }

    public String resignGame(String... params) {
        return null;
    }

    public String legalMoves(String... params) throws ResponseException {
        if (params.length >= 1) {
            var givenPosition = params[0];
            var startingPosition = generatePosition(givenPosition);
            return boardLayout(playerColor, true, startingPosition);
        }
        throw new ResponseException(400, "Expected: <POSITION>");
    }

    public String help() {
        return SET_TEXT_COLOR_BLUE + "redraw" + SET_TEXT_COLOR_WHITE + " - to display current board state\n" +
                SET_TEXT_COLOR_BLUE + "leave <GAMEID>" + SET_TEXT_COLOR_WHITE + " - to leave the game\n" +
                SET_TEXT_COLOR_BLUE + "move <START_POSITION><END_POSITION>" + SET_TEXT_COLOR_WHITE + " - to move one of your pieces\n" +
                SET_TEXT_COLOR_BLUE + "resign" + SET_TEXT_COLOR_WHITE + " - to forfeit the game\n" +
                SET_TEXT_COLOR_BLUE + "valid <POSITION>" + SET_TEXT_COLOR_WHITE + " - to show possible moves for a certain position\n" +
                SET_TEXT_COLOR_BLUE + "help" + SET_TEXT_COLOR_WHITE + " - with possible commands\n";
    }
}
