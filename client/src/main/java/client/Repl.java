package client;

import client.webSocket.NotificationHandler;
import webSocketMessages.serverMessages.ServerMessage;

import java.util.Scanner;
import static ui.EscapeSequences.*;

public class Repl implements NotificationHandler {
    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl, this);
    }

    public void run() {
        System.out.println(SET_TEXT_COLOR_WHITE + " \uD83D\uDC51 Welcome to 240 chess. Type Help to get started. \uD83D\uDC51");
        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    public void notify(ServerMessage serverMessage) {
        System.out.println(SET_TEXT_COLOR_GREEN + serverMessage.getServerMessageType().toString());
        printPrompt();
    }

    private void printPrompt() {
        State loggedIn = client.isLoggedIn();
        var startLine = (loggedIn.equals(State.SIGNEDIN)) ? "[LOGGED_IN]" : "[LOGGED_OUT]";
        System.out.print("\n" + ERASE_SCREEN + startLine + " >>> ");
    }
}
