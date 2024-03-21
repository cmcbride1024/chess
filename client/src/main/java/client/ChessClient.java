package client;

import ui.EscapeSequences;

import java.util.Arrays;

public class ChessClient {
    private final String serverUrl;
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;
    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public String eval(String input) {
        var tokens = input.toLowerCase().split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);

        return switch(cmd) {

            default -> help();
        };
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
}
