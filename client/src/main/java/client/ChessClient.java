package client;

import com.google.gson.Gson;

import exception.ResponseException;
import server.ServerFacade;
import model.*;

import java.util.Arrays;

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
            var gameName = params[0];
            var gameID = server.createGame(gameName).gameID();
            return String.format("Game '%s' has been created with ID: %d", gameName, gameID);
        }
        throw new ResponseException(400, "Expected: <NAME>");
    }

    public String listGames() throws ResponseException {
        assertSignedIn();
        var games = server.listGames();
        var result = new StringBuilder();
        var gson = new Gson();
        for (var game : games.gameList()) {
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
                server.joinGame(joinInformation);
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
                server.joinGame(joinInformation);
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
            return """
            register <USERNAME> <PASSWORD> <EMAIL> - to create an account
            login <USERNAME> <PASSWORD> - to play chess
            quit - playing chess
            help - with possible commands
            """;
        }
        return """
            create <NAME> - a game
            list - games
            join <ID> [WHITE|BLACK|<empty>] - a game
            observe <ID> - a game
            logout - when you are done
            quit - playing chess
            help - with possible commands
            """;
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
