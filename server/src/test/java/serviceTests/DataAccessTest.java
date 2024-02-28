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
    private static MemoryDataAccess getDataAccess() {
        return new MemoryDataAccess();
    }

    @Test
    void clearApplication() throws DataAccessException {
        MemoryDataAccess dataAccess = getDataAccess();
        UserService service = new UserService(dataAccess);

        UserData sampleUser = new UserData("steve01", "password", "steve@gmail.com");
        dataAccess.createUser(sampleUser);
        dataAccess.createAuth(sampleUser);

        String gameName = "test";
        Integer gameID = dataAccess.createGameId(gameName);
        ChessGame game = new ChessGame();
        dataAccess.createGame(new GameData(gameID, "white", "black", gameName, game));

        service.clearApplication();
        assertEquals(0, dataAccess.getGames().size());
        assertEquals(0, dataAccess.getAuths().size());
        assertEquals(0, dataAccess.getUsers().size());
    }
}
