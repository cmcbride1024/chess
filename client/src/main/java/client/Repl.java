package client;

import java.util.Scanner;
import client.*;

public class Repl {
    static String loggedOutHelp = """
            register <USERNAME> <PASSWORD> <EMAIL> - to create an account
            login <USERNAME> <PASSWORD> - to play chess
            quit - playing chess
            help - with possible commands
            
            """;
    static String loggedInHelp = """
            create <NAME> - a game
            list - games
            join <ID> [WHITE|BLACK|<empty>] - a game
            observe <ID> - a game
            logout - when you are done
            quit - playing chess
            help - with possible commands
            
            """;
    ChessClient client = new ChessClient();
    public static void main(String[] args) {
        boolean loggedIn = false;
        System.out.println(" \uD83D\uDC51 Welcome to 240 chess. Type Help to get started. \uD83D\uDC51");

        while (true) {
            String linePrefix = (loggedIn ? "[LOGGED_IN]" : "[LOGGED_OUT]");
            System.out.print(linePrefix + " >>> ");

            Scanner scanner = new Scanner(System.in);
            String sentence = scanner.nextLine();

            String[] words = sentence.trim().split("\\s+");

            String firstWord;
            if (words.length > 0) {
                firstWord = words[0];
            } else {
                firstWord = "";
            }

            switch(firstWord.toLowerCase()) {
                case "help" -> System.out.println(printHelpText(loggedIn));
                default -> System.out.println("Command not recognized. Type 'help' for list of commands.");
            };
        }
    }

    private static String printHelpText(boolean loggedIn) {
        return (loggedIn ? loggedInHelp : loggedOutHelp);
    }
}
