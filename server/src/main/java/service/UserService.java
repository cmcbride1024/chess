package service;

import dataAccess.DataAccessException;
import dataAccess.MemoryDataAccess;
import model.AuthData;
import model.UserData;

public class UserService {
    private final MemoryDataAccess dataAccess;

    public UserService(MemoryDataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData user) throws DataAccessException {
        var existingUser = dataAccess.getUser(user.username());

        if (existingUser == null) {
            dataAccess.createUser(user);
            return dataAccess.createAuth(user);
        } else {
            throw new DataAccessException("Username already exists in the database.");
        }
    }

    public AuthData login(UserData user) {
        return null;
    }
    public void logout(UserData user) {
        return;
    }

    public void clearApplication() throws DataAccessException {
        dataAccess.clearUsers();
        dataAccess.clearGames();
        dataAccess.clearAuthTokens();
    }
}
