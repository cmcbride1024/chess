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
    public void resetDatabase() throws ResponseException {
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
    void createGamesUnauthorized() {
        assertThrows(
                ResponseException.class,
                () -> facade.createGame(new GameName("game1"), UUID.randomUUID().toString()),
                "server should not create a game for an unauthorized user."
        );
    }

    @Test
    void listGames() throws ResponseException {
        var authData = facade.register(sampleData);
        facade.createGame(new GameName("game1"), authData.authToken());
        var games = facade.listGames(authData.authToken());
        assertFalse(games.gameList().isEmpty());
    }

    @Test
    void listGamesUnauthorized() {
        assertThrows(
                ResponseException.class,
                () -> facade.listGames(UUID.randomUUID().toString()),
                "server should not list games for an unauthorized user."
        );
    }

    @Test
    void joinGame() throws ResponseException {
        var authData = facade.register(sampleData);
        var id = facade.createGame(new GameName("game1"), authData.authToken());
        var joinInformation = new JoinInformation("white", id.gameID());
        facade.joinGame(joinInformation, authData.authToken());
        var games = facade.listGames(authData.authToken());
        for (var game : games.gameList()) {
            assertEquals(game.whiteUsername(), sampleData.username());
        }
    }

    @Test
    void joinGameUnauthorized() throws ResponseException {
        var authData = facade.register(sampleData);
        var id = facade.createGame(new GameName("game1"), authData.authToken());
        var joinInformation = new JoinInformation("white", id.gameID());
        assertThrows(
                ResponseException.class,
                () -> facade.joinGame(joinInformation, UUID.randomUUID().toString()),
                "server should not let an unauthorized user join a game."
        );

        var newJoinInformation = new JoinInformation("white", 1515);
        assertThrows(
                ResponseException.class,
                () -> facade.joinGame(newJoinInformation, authData.authToken()),
                "server should not you join a non-existent game."
        );
    }

    @Test
    void logout() throws ResponseException {
        var authData = facade.register(sampleData);
        facade.logout(authData.authToken());
        assertThrows(
                ResponseException.class,
                () -> facade.listGames(authData.authToken()),
                "server should not list games for someone who logged in then logged out."
        );
    }

    @Test
    void logoutNonExistentUser() {
        assertThrows(
                ResponseException.class,
                () -> facade.logout(UUID.randomUUID().toString()),
                "server shouldn't let someone not registered log out."
        );
    }

    @Test
    void clearDatabase() throws ResponseException {
        facade.register(sampleData);
        facade.clearDatabase();
        var loginRequest = new LoginRequest(sampleData.username(), sampleData.password());
        assertThrows(
                ResponseException.class,
                () -> facade.login(loginRequest),
                "clearing the database should remove login credentials."
        );
    }

    @Test
    void clearEmptyDatabase() throws ResponseException {
        facade.clearDatabase();
        var authData = facade.register(sampleData);
        var games = facade.listGames(authData.authToken());
        assertEquals(0, games.gameList().size());
    }
}
