package clientTests;

import org.junit.jupiter.api.*;
import client.Server;
import client.ServerFacade;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    void register() {
        var authData = facade.register("stevejobs", "lotsofmoney", "sjobs@icloud.com");
        assertTrue(authData.authToken().length() > 10);
    }
}
