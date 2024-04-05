package client;

import com.google.gson.Gson;

import exception.ResponseException;
import server.ServerFacade;
import model.*;
import static ui.EscapeSequences.*;


import java.util.Arrays;
import java.util.Objects;

public class ChessClient {
    private String loggedInUser = null;
    private AuthData authData = null;
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);

            return switch(cmd) {
                case "login" -> login(params);
                case "register" -> register(params);
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);
                case "logout" -> logout();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String login(String... params) throws ResponseException {
        if (params.length >= 2) {
            var username = params[0];
            var password = params[1];
            var userLogin = new LoginRequest(username, password);
            authData = server.login(userLogin);
            loggedInUser = username;
            state = State.SIGNEDIN;
            return String.format("Welcome %s", username);
        }
        throw new ResponseException(400, "Expected: <USERNAME> <PASSWORD>");
    }

    public String register(String... params) throws ResponseException {
        if (params.length >= 3) {
            var username = params[0];
            var password = params[1];
            var email = params[2];
            var newUser = new UserData(username, password, email);
            authData = server.register(newUser);
            loggedInUser = username;
            state = State.SIGNEDIN;
            return String.format("Account has been created for %s. You are now logged in", username);
        }
        throw new ResponseException(400, "Expected: <USERNAME> <PASSWORD> <EMAIL>");
    }

    public String createGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length >= 1) {
            var name = params[0];
            var gameName = new GameName(name);
            var gameID = server.createGame(gameName, authData.authToken()).gameID();
            return String.format("Game '%s' has been created with ID: %d", gameName.gameName(), gameID);
        }
        throw new ResponseException(400, "Expected: <NAME>");
    }

    public String listGames() throws ResponseException {
        assertSignedIn();
        var games = server.listGames(authData.authToken());
        var result = new StringBuilder();
        var gson = new Gson();
        for (var game : games.games()) {
            result.append(gson.toJson(game)).append('\n');
        }
        return result.toString();
    }

    public String joinGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length >= 1) {
            try {
                var gameID = Integer.parseInt(params[0]);
                String playerColor = null;
                if (params.length == 2) {
                    playerColor = params[1];
                }
                var joinInformation = new JoinInformation(playerColor, gameID);
                server.joinGame(joinInformation, authData.authToken());
                System.out.println(boardLayout("white"));
                System.out.println(boardLayout("black"));
                var joinedUser = (playerColor != null) ? playerColor : "observer";
                return String.format("Joined game %d as %s", gameID, joinedUser);
            } catch (NumberFormatException ignored) {
            }
        }
        throw new ResponseException(400, "Expected: <ID> [WHITE|BLACK|<empty>]");
    }

    public String observeGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length >= 1) {
            try {
                var gameID = Integer.parseInt(params[0]);
                var joinInformation = new JoinInformation(null, gameID);
                server.joinGame(joinInformation, authData.authToken());
                System.out.println(boardLayout("white"));
                System.out.println(boardLayout("black"));
                return String.format("Observing game %d", gameID);
            } catch (NumberFormatException ignored) {
            }
        }
        throw new ResponseException(400, "Expected: <ID>");
    }

    public String logout() throws ResponseException {
        assertSignedIn();
        var leavingUser = loggedInUser;
        loggedInUser = null;
        server.logout(authData.authToken());
        authData = null;
        state = State.SIGNEDOUT;
        return String.format("%s logged out successfully", leavingUser);
    }

    public String help() {
        if (state == State.SIGNEDOUT) {
            return SET_TEXT_COLOR_BLUE + "register <USERNAME> <PASSWORD> <EMAIL>" + SET_TEXT_COLOR_WHITE + " - to create an account\n" +
                SET_TEXT_COLOR_BLUE + "login <USERNAME> <PASSWORD>" + SET_TEXT_COLOR_WHITE + " - to play chess\n" +
                SET_TEXT_COLOR_BLUE + "quit" + SET_TEXT_COLOR_WHITE + " - playing chess\n" +
                SET_TEXT_COLOR_BLUE + "help" + SET_TEXT_COLOR_WHITE + " - with possible commands\n";
        }
        return SET_TEXT_COLOR_BLUE + "create <NAME>" + SET_TEXT_COLOR_WHITE + " - a game\n" +
            SET_TEXT_COLOR_BLUE + "list" + SET_TEXT_COLOR_WHITE + " - games\n" +
            SET_TEXT_COLOR_BLUE + "join <ID> [WHITE|BLACK|<empty>]" + SET_TEXT_COLOR_WHITE + " - a game\n" +
            SET_TEXT_COLOR_BLUE + "observe <ID>" + SET_TEXT_COLOR_WHITE + " - a game\n" +
            SET_TEXT_COLOR_BLUE + "logout" + SET_TEXT_COLOR_WHITE + " - when you are done\n" +
            SET_TEXT_COLOR_BLUE + "quit" + SET_TEXT_COLOR_WHITE + " - playing chess\n" +
            SET_TEXT_COLOR_BLUE + "help" + SET_TEXT_COLOR_WHITE + " - with possible commands\n";
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

    private static String getLabel(int row, int col) {
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

    public State isLoggedIn() {
        return state;
    }

    private void assertSignedIn() throws ResponseException {
        if (state == State.SIGNEDOUT) {
            throw new ResponseException(400, "You must sign in.");
        }
    }
}
