package clientTests;

import exception.ResponseException;
import model.*;
import org.junit.jupiter.api.*;
import client.Server;
import server.ServerFacade;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;
    private final UserData sampleData = new UserData("bob", "password", "bob@gmail.com");

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @BeforeEach
    public void clearDatabase() throws ResponseException {
        facade.clearDatabase();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    void register() throws ResponseException {
        var authData = facade.register(sampleData);
        assertTrue(authData.authToken().length() > 10);
        assertNotNull(authData.username());
    }

    @Test
    void registerExistingUser() throws ResponseException {
        facade.register(sampleData);
        assertThrows(
                ResponseException.class,
                () -> facade.register(sampleData),
                "server should not register an existing user."
        );
    }

    @Test
    void login() throws ResponseException {
        facade.register(sampleData);
        LoginRequest loginRequest = new LoginRequest(sampleData.username(), sampleData.password());
        var authData = facade.login(loginRequest);
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    void loginNonExistingUser() {
        LoginRequest loginRequest = new LoginRequest(sampleData.username(), sampleData.password());
        assertThrows(
                ResponseException.class,
                () -> facade.login(loginRequest),
                "server should not login a non-registered user."
        );
    }

    @Test
    void createGame() throws ResponseException {
        var authData = facade.register(sampleData);
        var id = facade.createGame(new GameName("game1"), authData.authToken());
        assertNotNull(id.gameID());
    }

    @Test
    void createGameWithoutAuthorization() {
        assertThrows(
                ResponseException.class,
                () -> facade.createGame(new GameName("game1"), UUID.randomUUID().toString()),
                "server should not create a game random UUID."
        );
    }
}
