package dataAccessTests;

import chess.ChessGame;
import dataAccess.*;
import exception.ResponseException;
import model.UserData;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import service.UserService;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DataAccessTest {
    private DataAccess getDataAccess(Class<? extends DataAccess> databaseClass) throws ResponseException, DataAccessException, SQLException {
        DataAccess db;
        if (databaseClass.equals(MySqlDataAccess.class)) {
            db = new MySqlDataAccess();
        } else {
            db = new MemoryDataAccess();
        }
        db.clearAuthTokens();
        db.clearGames();
        db.clearUsers();
        db.clearGameIDs();

        return db;
    }

    @ParameterizedTest
    @ValueSource(classes = {MySqlDataAccess.class, MemoryDataAccess.class})
    void registerUser(Class<? extends DataAccess> dbClass) throws UnauthorizedException, DataAccessException, ResponseException, SQLException {
        DataAccess dataAccess = getDataAccess(dbClass);
        UserService service = new UserService(dataAccess);

        UserData testUser = new UserData("stevejobs", "apple", "steve@icloud.com");
        AuthData authData = service.register(testUser);
        assertEquals(1, dataAccess.getAuths().size());
        assertNotNull(dataAccess.getAuth(authData.authToken()));
    }

    @ParameterizedTest
    @ValueSource(classes = {MySqlDataAccess.class, MemoryDataAccess.class})
    void registerExistingUser(Class<? extends DataAccess> dbClass) throws UnauthorizedException, DataAccessException, ResponseException, SQLException {
        DataAccess dataAccess = getDataAccess(dbClass);
        UserService service = new UserService(dataAccess);

        UserData testUser = new UserData("stevejobs", "apple", "steve@icloud.com");
        service.register(testUser);

        try {
            service.register(testUser);
            fail("Service registered an existing user");
        } catch (UnauthorizedException e) {
            assertEquals(1, dataAccess.getAuths().size());
        }
    }

    @ParameterizedTest
    @ValueSource(classes = {MySqlDataAccess.class, MemoryDataAccess.class})
    void loginUser(Class<? extends DataAccess> dbClass) throws DataAccessException, UnauthorizedException, ResponseException, SQLException {
        DataAccess dataAccess = getDataAccess(dbClass);
        UserService service = new UserService(dataAccess);

        UserData testUser = new UserData("lebron", "leking", "lebron@aol.com");
        service.register(testUser);

        AuthData authData = service.login(testUser.username(), testUser.password());
        assertEquals(2, dataAccess.getAuths().size());
        assertNotNull(dataAccess.getAuth(authData.authToken()));
    }

    @ParameterizedTest
    @ValueSource(classes = {MySqlDataAccess.class, MemoryDataAccess.class})
    void loginNonExistentUser(Class<? extends DataAccess> dbClass) throws DataAccessException, ResponseException, SQLException {
        DataAccess dataAccess = getDataAccess(dbClass);
        UserService service = new UserService(dataAccess);

        assertEquals(0, dataAccess.getAuths().size());
        UserData testUser = new UserData("lebron", "leking", "lebron@aol.com");

        try {
            service.login(testUser.username(), testUser.password());
            fail("User has not been registered first.");
        } catch (UnauthorizedException e) {
            assertEquals(0, dataAccess.getAuths().size());
        }
    }

    @ParameterizedTest
    @ValueSource(classes = {MySqlDataAccess.class, MemoryDataAccess.class})
    void logoutUser(Class<? extends DataAccess> dbClass) throws UnauthorizedException, DataAccessException, ResponseException, SQLException {
        DataAccess dataAccess = getDataAccess(dbClass);
        UserService service = new UserService(dataAccess);

        UserData testUser = new UserData("Bob", "Marley", "bobmarley@yahoo.com");
        AuthData testAuth = service.register(testUser);

        service.logout(testAuth.authToken());
        assertEquals(0, dataAccess.getAuths().size());
    }

    @ParameterizedTest
    @ValueSource(classes = {MySqlDataAccess.class, MemoryDataAccess.class})
    void logoutNonExistentUser(Class<? extends DataAccess> dbClass) throws DataAccessException, ResponseException, SQLException {
        DataAccess dataAccess = getDataAccess(dbClass);
        UserService service = new UserService(dataAccess);

        try {
            service.logout(UUID.randomUUID().toString());
            fail("Service shouldn't log out user who isn't logged in.");
        } catch (UnauthorizedException e) {
            assertEquals(0, dataAccess.getAuths().size());
        }
    }

    @ParameterizedTest
    @ValueSource(classes = {MySqlDataAccess.class, MemoryDataAccess.class})
    void listGames(Class<? extends DataAccess> dbClass) throws UnauthorizedException, DataAccessException, ResponseException, SQLException {
        DataAccess dataAccess = getDataAccess(dbClass);
        UserService service = new UserService(dataAccess);

        UserData testUser = new UserData("Bob", "Marley", "bobmarley@yahoo.com");
        AuthData testAuth = service.register(testUser);
        service.createGame(testAuth.authToken(), "game1");

        UserData testUserTwo = new UserData("lebron", "leking", "lebron@aol.com");
        AuthData testAuthTwo = service.register(testUserTwo);
        service.createGame(testAuthTwo.authToken(), "game2");

        var gameList = service.listGames(testAuth.authToken());
        assertEquals(2, gameList.size());
        assertEquals(gameList.size(), dataAccess.getGames().size());
    }

    @ParameterizedTest
    @ValueSource(classes = {MySqlDataAccess.class, MemoryDataAccess.class})
    void listGamesNotAuthorized(Class<? extends DataAccess> dbClass) throws DataAccessException, ResponseException, SQLException {
        DataAccess dataAccess = getDataAccess(dbClass);
        UserService service = new UserService(dataAccess);

        try {
            service.listGames(UUID.randomUUID().toString());
            fail("Service shouldn't list games for user who isn't registered.");
        } catch (UnauthorizedException e) {
            assertEquals(0, dataAccess.getAuths().size());
        }
    }

    @ParameterizedTest
    @ValueSource(classes = {MySqlDataAccess.class, MemoryDataAccess.class})
    void createGame(Class<? extends DataAccess> dbClass) throws UnauthorizedException, DataAccessException, ResponseException, SQLException {
        DataAccess dataAccess = getDataAccess(dbClass);
        UserService service = new UserService(dataAccess);

        UserData testUser = new UserData("Bob", "Marley", "bobmarley@yahoo.com");
        AuthData testAuth = service.register(testUser);
        service.createGame(testAuth.authToken(), "game1");

        assertEquals(1, dataAccess.getGames().size());
    }

    @ParameterizedTest
    @ValueSource(classes = {MySqlDataAccess.class, MemoryDataAccess.class})
    void createGameNotAuthorized(Class<? extends DataAccess> dbClass) throws ResponseException, SQLException, DataAccessException {
        DataAccess dataAccess = getDataAccess(dbClass);
        UserService service = new UserService(dataAccess);

        try {
            service.createGame(UUID.randomUUID().toString(), "game1");
            fail("Service shouldn't log out user who isn't logged in.");
        } catch (UnauthorizedException | DataAccessException e) {
            assertEquals(0, dataAccess.getAuths().size());
        }
    }

    @ParameterizedTest
    @ValueSource(classes = {MySqlDataAccess.class, MemoryDataAccess.class})
    void joinGame(Class<? extends DataAccess> dbClass) throws DataAccessException, UnauthorizedException, InvalidGameID, ResponseException, SQLException {
        DataAccess dataAccess = getDataAccess(dbClass);
        UserService service = new UserService(dataAccess);

        UserData testUser = new UserData("Steve", "Martin", "steve@gmail.com");
        AuthData testAuth = service.register(testUser);

        service.createGame(testAuth.authToken(), "game1");
        service.joinGame(testAuth.authToken(), "WHITE", 1);
    }

    @ParameterizedTest
    @ValueSource(classes = {MySqlDataAccess.class, MemoryDataAccess.class})
    void joinGameNotAuthorized(Class<? extends DataAccess> dbClass) throws DataAccessException, InvalidGameID, ResponseException, SQLException {
        DataAccess dataAccess = getDataAccess(dbClass);
        UserService service = new UserService(dataAccess);

        try {
            var testAuth = new AuthData(UUID.randomUUID().toString(), "carlos");
            service.createGame(testAuth.authToken(), "game1");
            service.joinGame(testAuth.authToken(), "WHITE", 1);

            fail("Service shouldn't allow user to join game who isn't logged in.");
        } catch (UnauthorizedException e) {
            assertEquals(0, dataAccess.getGames().size());
        }
    }

    @ParameterizedTest
    @ValueSource(classes = {MySqlDataAccess.class, MemoryDataAccess.class})
    void joinGameWrongID(Class<? extends DataAccess> dbClass) throws UnauthorizedException, DataAccessException, ResponseException, SQLException {
        DataAccess dataAccess = getDataAccess(dbClass);
        UserService service = new UserService(dataAccess);

        try {
            UserData testUser = new UserData("Steve", "Martin", "steve@gmail.com");
            AuthData testAuth = service.register(testUser);
            service.createGame(testAuth.authToken(), "game1");
            service.joinGame(testAuth.authToken(), "BLACK", 2);

            fail("Service shouldn't allow user to join game with invalid ID.");
        } catch (InvalidGameID e) {
            assertEquals(1, dataAccess.getGames().size());
        }
    }

    @ParameterizedTest
    @ValueSource(classes = {MySqlDataAccess.class, MemoryDataAccess.class})
    void clearApplication(Class<? extends DataAccess> dbClass) throws DataAccessException, ResponseException, SQLException {
        DataAccess dataAccess = getDataAccess(dbClass);
        UserService service = new UserService(dataAccess);

        UserData testUser = new UserData("steve01", "password", "steve@gmail.com");
        dataAccess.createUser(testUser);
        dataAccess.createAuth(testUser);

        String gameName = "test";
        Integer gameID = dataAccess.createGameID(gameName);
        ChessGame game = new ChessGame();
        dataAccess.createGame(new GameData(gameID, "white", "black", gameName, game));

        service.clearApplication();
        assertEquals(0, dataAccess.getGames().size());
        assertEquals(0, dataAccess.getAuths().size());
    }

    @ParameterizedTest
    @ValueSource(classes = {MySqlDataAccess.class, MemoryDataAccess.class})
    void clearEmptyApplication(Class<? extends DataAccess> dbClass) throws DataAccessException, ResponseException, SQLException {
        DataAccess dataAccess = getDataAccess(dbClass);
        UserService service = new UserService(dataAccess);

        // Verify that database is already empty
        assertEquals(0, dataAccess.getGames().size());
        assertEquals(0, dataAccess.getAuths().size());

        // Verify that clearing empty tables doesn't throw an error
        service.clearApplication();
    }
}
