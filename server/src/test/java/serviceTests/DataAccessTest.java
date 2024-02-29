package serviceTests;

import chess.ChessGame;
import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import dataAccess.MemoryDataAccess;
import model.UserData;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.Test;
import service.UserService;

import javax.xml.crypto.Data;
import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DataAccessTest {
    MemoryDataAccess dataAccess = getDataAccess();
    UserService service = new UserService(dataAccess);
    private static MemoryDataAccess getDataAccess() {
        return new MemoryDataAccess();
    }

    @Test
    void registerUser() throws DataAccessException {
        UserData testUser = new UserData("stevejobs", "apple", "steve@icloud.com");

        AuthData authData = service.register(testUser);
        var users = dataAccess.getUsers();
        assertEquals(1, users.size());
        assertTrue(users.containsValue(testUser));
        assertEquals(1, dataAccess.getAuths().size());
        assertTrue(dataAccess.getAuths().containsValue(authData));
    }

    @Test
    void registerExistingUser() throws DataAccessException {
        UserData testUser = new UserData("stevejobs", "apple", "steve@icloud.com");
        service.register(testUser);

        try {
            service.register(testUser);
            fail("Service registered an existing user");
        } catch (DataAccessException e) {
            assertEquals(1, dataAccess.getUsers().size());
            assertEquals(1, dataAccess.getAuths().size());
        }
    }

    @Test
    void loginUser() throws DataAccessException {
        UserData testUser = new UserData("lebron", "leking", "lebron@aol.com");
        service.register(testUser);

        AuthData authData = service.login(testUser);
        assertEquals(1, dataAccess.getAuths().size());
        assertTrue(dataAccess.getAuths().containsValue(authData));
    }

    @Test
    void loginNonExistentUser() throws DataAccessException {
        assertEquals(0, dataAccess.getAuths().size());
        UserData testUser = new UserData("lebron", "leking", "lebron@aol.com");

        try {
            service.login(testUser);
            fail("User has not been registered first.");
        } catch (DataAccessException e) {
            assertEquals(0, dataAccess.getAuths().size());
        }
    }

    @Test
    void logoutUser() throws DataAccessException {
        UserData testUser = new UserData("Bob", "Marley", "bobmarley@yahoo.com");
        service.register(testUser);
        AuthData testAuth = service.login(testUser);

        service.logout(testAuth);
        assertEquals(0, dataAccess.getAuths().size());
    }

    @Test
    void logoutNonExistentUser() throws DataAccessException {
        try {
            service.logout(new AuthData(UUID.randomUUID().toString(), "Bob"));
            fail("Service shouldn't log out user who isn't logged in.");
        } catch (DataAccessException e) {
            assertEquals(0, dataAccess.getAuths().size());
        }
    }

    @Test
    void listGames() throws DataAccessException {
        UserData testUser = new UserData("Bob", "Marley", "bobmarley@yahoo.com");
        AuthData testAuth = service.register(testUser);
        service.createGame(testAuth, "game1");

        UserData testUserTwo = new UserData("lebron", "leking", "lebron@aol.com");
        AuthData testAuthTwo = service.register(testUserTwo);
        service.createGame(testAuthTwo, "game2");

        var gameList = service.listGames(testAuth);
        assertEquals(2, gameList.size());
        assertEquals(gameList.size(), dataAccess.getGames().size());
    }

    @Test
    void listGamesNotAuthorized() throws DataAccessException {
        try {
            service.listGames(new AuthData(UUID.randomUUID().toString(), "steve"));
            fail("Service shouldn't list games for user who isn't registered.");
        } catch (DataAccessException e) {
            assertEquals(0, dataAccess.getAuths().size());
        }
    }

    @Test
    void createGame() throws DataAccessException {
        UserData testUser = new UserData("Bob", "Marley", "bobmarley@yahoo.com");
        AuthData testAuth = service.register(testUser);
        service.createGame(testAuth, "game1");

        assertEquals(1, dataAccess.getGames().size());
    }

    @Test
    void createGameNotAuthorized() throws DataAccessException {
        try {
            service.createGame(new AuthData(UUID.randomUUID().toString(), "steve"), "game1");
            fail("Service shouldn't log out user who isn't logged in.");
        } catch (DataAccessException e) {
            assertEquals(0, dataAccess.getAuths().size());
        }
    }

    @Test
    void joinGame() throws DataAccessException {

    }

    @Test
    void joinGameNotAuthorized() throws DataAccessException {

    }

    @Test
    void joinGameWrongID() throws DataAccessException {

    }

    @Test
    void clearApplication() throws DataAccessException {
        UserData testUser = new UserData("steve01", "password", "steve@gmail.com");
        dataAccess.createUser(testUser);
        dataAccess.createAuth(testUser);

        String gameName = "test";
        Integer gameID = dataAccess.createGameId(gameName);
        ChessGame game = new ChessGame();
        dataAccess.createGame(new GameData(gameID, "white", "black", gameName, game));

        service.clearApplication();
        assertEquals(0, dataAccess.getGames().size());
        assertEquals(0, dataAccess.getAuths().size());
        assertEquals(0, dataAccess.getUsers().size());
    }

    @Test
    void clearEmptyApplication() throws DataAccessException {
        // Verify that database is already empty
        assertEquals(0, dataAccess.getGames().size());
        assertEquals(0, dataAccess.getAuths().size());
        assertEquals(0, dataAccess.getUsers().size());

        // Verify that clearing empty tables doesn't throw an error
        clearApplication();
    }
}
