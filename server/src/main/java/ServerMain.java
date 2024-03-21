import client.*;

public class ServerMain {
    public static void main(String[] args) {
        try {
            int port = 8080;
            if (args.length >= 1) {
                port = Integer.parseInt(args[0]);
            }

            var portNumber = new Server().run(port);
            System.out.printf("Server started on port %d%n", portNumber);
        } catch (Throwable ex) {
            System.out.printf("Unable to start server: %s%n", ex.getMessage());
        }
    }
}