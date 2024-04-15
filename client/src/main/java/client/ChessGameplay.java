package client;

import chess.*;
import client.webSocket.NotificationHandler;
import client.webSocket.WebSocketFacade;
import com.google.gson.Gson;
import exception.ResponseException;
import model.AuthData;
import webSocketMessages.serverMessages.LoadGame;
import webSocketMessages.serverMessages.Error;
import webSocketMessages.serverMessages.Notification;
import webSocketMessages.serverMessages.ServerMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static ui.EscapeSequences.*;

public class ChessGameplay implements NotificationHandler {
    private ChessGame gameState;
    private final ChessGame.TeamColor playerColor;
    private final ChessClient client;
    private final WebSocketFacade ws;
    private final AuthData authData;
    private final int gameID;

    public ChessGameplay(ChessClient client, ChessGame.TeamColor playerColor, String serverUrl, AuthData authData, int gameID) throws ResponseException {
        this.playerColor = playerColor;
        this.client = client;
        ws = new WebSocketFacade(serverUrl, this);
        this.authData = authData;
        this.gameID = gameID;
    }

    public String eval(String... tokens) throws ResponseException {
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);

        return switch(cmd) {
            case "redraw" -> redrawBoard();
            case "leave" -> leaveGame(params);
            case "move" -> makeMove(params);
            case "resign" -> resignGame();
            case "valid" -> legalMoves(params);
            default -> help();
        };
    }

    private String getLabel(int row, int col, boolean colored) {
        String[] columnLabels = {" ", "a", "b", "c", "d", "e", "f", "g", "h", " "};
        String[] rowLabels = {" ", "1", "2", "3", "4", "5", "6", "7", "8", " "};

        // Board labels
        if (row == 0 || row == 9) {
            return SET_TEXT_COLOR_WHITE + columnLabels[col] + RESET_TEXT_COLOR;
        } else if (col == 0 || col == 9) {
            return SET_TEXT_COLOR_WHITE + rowLabels[row] + RESET_TEXT_COLOR;
        }

        // Get piece at board position
        ChessBoard board = getGameState().getBoard();
        ChessPiece tempPiece = board.getPiece(new ChessPosition(row, col));
        if (tempPiece == null) {
            return " ";
        }

        // Return the symbol and color for the found piece
        String pieceSymbol = switch (tempPiece.getPieceType()) {
            case ROOK -> "R";
            case KING -> "K";
            case QUEEN -> "Q";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case PAWN -> "P";
        };
        if (colored) {
            return (tempPiece.getTeamColor() == ChessGame.TeamColor.WHITE) ? SET_TEXT_COLOR_WHITE + pieceSymbol + RESET_TEXT_COLOR
                    : SET_TEXT_COLOR_BLACK + pieceSymbol + RESET_TEXT_COLOR;
        }
        return pieceSymbol;
    }

    public String boardLayout(ChessGame.TeamColor playerColor, boolean highlight, ChessPosition position) {
        StringBuilder board = new StringBuilder();
        for (int row = 0; row <= 9; row++) {
            for (int col = 0; col <= 9; col++) {
                int displayRow = (Objects.equals(playerColor, ChessGame.TeamColor.BLACK)) ? row : 9 - row;
                int displayCol = (Objects.equals(playerColor, ChessGame.TeamColor.BLACK)) ? 9 - col : col;

                String chessCharacter = getLabel(displayRow, displayCol, true);
                boolean isDark = (displayRow + displayCol) % 2 == 0;
                String bgColor = isDark ? SET_BG_COLOR + "95m" : SET_BG_COLOR + "222m";

                if (row == 0 || col == 0 || row == 9 || col == 9) {
                    board.append(SET_BG_COLOR_LIGHT_GREY);

                } else {
                    boolean containsSquare = false;

                    if (highlight) {
                        var validMoves = getGameState().validMoves(position);
                        for (var move : validMoves) {
                            var validMovePos = new ChessPosition(displayRow, displayCol);
                            if (move.getEndPosition().equals(validMovePos)) {
                                containsSquare = true;
                                var tempPiece = gameState.getBoard().getPiece(validMovePos);
                                if (tempPiece != null && tempPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                                    chessCharacter = SET_TEXT_COLOR_BLACK + getLabel(displayRow, displayCol, false) + RESET_TEXT_COLOR;
                                }
                                break;
                            }
                        }
                        if (position.equals(new ChessPosition(displayRow, displayCol))) {
                            bgColor = SET_BG_COLOR_YELLOW;
                        } else if (containsSquare) {
                            bgColor = isDark ? SET_BG_COLOR_DARK_GREEN : SET_BG_COLOR_GREEN;
                        }

                    }
                    board.append(bgColor);
                }
                board.append(String.format(" %s ", chessCharacter)).append("\u001B[49m");
            }
            board.append('\n').append(SET_TEXT_COLOR_WHITE).append("\u001B[49m");
        }
        return board.toString();
    }

    public void joinGame() throws ResponseException {
        ws.joinGame(gameID, playerColor, authData);
    }

    public void observeGame() throws ResponseException {
        ws.observeGame(gameID, authData);
    }

    public String redrawBoard() {
        return boardLayout(playerColor, false, null);
    }

    public String leaveGame(String... params) throws ResponseException {
        if (params.length >= 1) {
            int gameID = Integer.parseInt(params[0]);
            ws.leaveGame(gameID, authData);
            client.setState(State.SIGNEDIN);

            return String.format("%s has left game %d", authData.username(), gameID);
        }
        throw new ResponseException(400, "Expected: <GAME_ID>");
    }

    private ChessPosition generatePosition(String position) throws ResponseException {
        try {
            char columnChar = Character.toLowerCase(position.charAt(0));
            char rowChar = position.charAt(1);
            int col = columnChar - 'a' + 1;
            int row = rowChar - '1' + 1;

            return new ChessPosition(row, col);
        } catch (Exception ex) {
            throw new ResponseException(400, ex.getMessage());
        }
    }

    public String makeMove(String... params) throws ResponseException {
        if (client.getState().equals(State.OBSERVING)) {
            throw new ResponseException(400, "You cannot make moves as an observer");
        } else if (gameState.getGameIsOver()) {
            throw new ResponseException(400, "Game is already over");
        }

        if (params.length >= 1) {
            String move = params[0];
            if (move.length() >= 4) {
                String startPos = move.substring(0, 2);
                String endPos = move.substring(2, 4);
                Map<String, ChessPiece.PieceType> notationPieces = new HashMap<>() {{
                    put("Q", ChessPiece.PieceType.QUEEN); put("N", ChessPiece.PieceType.KNIGHT);
                    put("R", ChessPiece.PieceType.KNIGHT); put("B", ChessPiece.PieceType.BISHOP);
                }};

                var promotionPiece = (params.length >= 2) ? notationPieces.get(params[1].toUpperCase()) : null;
                var startPosition = generatePosition(startPos);
                var endPosition = generatePosition(endPos);
                var newMove = new ChessMove(startPosition, endPosition, promotionPiece);
                ws.makeMove(gameID, newMove, authData);

                String movingPiece = gameState.getBoard().getPiece(startPosition).toString();
                return String.format("Moved %s %s to %s", startPos, movingPiece, endPos);
            }
        }
        throw new ResponseException(400, "Expected: <START_POSITION><END_POSITION> <PROMOTION_PIECE (opt.)>");
    }

    public String resignGame() throws ResponseException {
        ws.resign(gameID, authData);

        return "";
    }

    public String legalMoves(String... params) throws ResponseException {
        if (params.length >= 1) {
            var givenPosition = params[0];
            var startingPosition = generatePosition(givenPosition);

            var tempPiece = gameState.getBoard().getPiece(startingPosition);
            if (tempPiece != null && !tempPiece.getTeamColor().equals(gameState.getTeamTurn())) {
                throw new ResponseException(400, String.format("It is %s's turn", gameState.getTeamTurn()));
            }
            return boardLayout(playerColor, true, startingPosition);
        }
        throw new ResponseException(400, "Expected: <POSITION>");
    }

    private void setGameState(ChessGame newGame) {
        gameState = newGame;
    }

    private ChessGame getGameState() {
        return gameState;
    }

    public String help() {
        return SET_TEXT_COLOR_BLUE + "redraw" + SET_TEXT_COLOR_WHITE + " - to display current board state\n" +
                SET_TEXT_COLOR_BLUE + "leave <GAME_ID>" + SET_TEXT_COLOR_WHITE + " - to leave the game\n" +
                SET_TEXT_COLOR_BLUE + "move <START_POSITION><END_POSITION> <PROMOTION_PIECE (opt.)>" + SET_TEXT_COLOR_WHITE + " - to move one of your pieces\n" +
                SET_TEXT_COLOR_BLUE + "resign" + SET_TEXT_COLOR_WHITE + " - to forfeit the game\n" +
                SET_TEXT_COLOR_BLUE + "valid <POSITION>" + SET_TEXT_COLOR_WHITE + " - to show possible moves for a certain position\n" +
                SET_TEXT_COLOR_BLUE + "help" + SET_TEXT_COLOR_WHITE + " - with possible commands\n";
    }

    private void printPrompt() {
        State loggedIn = client.getState();
        var startLine = switch(loggedIn) {
            case State.GAMEPLAY -> "[PLAYING]";
            case State.OBSERVING -> "[OBSERVING]";
            case State.SIGNEDIN -> "[LOGGED_IN]";
            default -> "";
        };
        System.out.print("\n" + ERASE_SCREEN + startLine + " >>> ");
    }

    @Override
    public void notify(String message) {
        ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
        ServerMessage.ServerMessageType messageType = serverMessage.getServerMessageType();
        switch (messageType) {
            case LOAD_GAME -> {
                LoadGame loadGame = new Gson().fromJson(message, LoadGame.class);
                setGameState(loadGame.getGame());
                System.out.println();
                System.out.println(boardLayout(playerColor, false, null));
                printPrompt();
            }
            case ERROR -> {
                Error error = new Gson().fromJson(message, Error.class);
                System.out.println();
                System.out.println(SET_TEXT_COLOR_RED + error.getErrorMessage() + SET_TEXT_COLOR_WHITE);
                printPrompt();
            }
            case NOTIFICATION -> {
                Notification notification = new Gson().fromJson(message, Notification.class);
                System.out.println();
                System.out.println(SET_TEXT_COLOR_GREEN + notification.getMessage() + SET_TEXT_COLOR_WHITE);
                printPrompt();
            }
        }
    }
}
