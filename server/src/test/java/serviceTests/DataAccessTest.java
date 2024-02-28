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

        service.register(testUser);
        var users = dataAccess.getUsers();
        assertEquals(1, users.size());
        assert users.containsValue(testUser);
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
        }
    }

    @Test
    void joinGame() throws DataAccessException {

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
