package dataAccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.HashMap;
import java.util.Collection;
import java.util.UUID;

public class MemoryDataAccess implements DataAccess {
    private int nextId = 1;
    private final HashMap<Integer, UserData> users = new HashMap<>();

    public UserData createUser(UserData u) {
        var user = new UserData(u.username(), u.password(), u.email());
        users.put(nextId++, user);

        return user;
    }

    public void createAuth(UserData u) {
    }

    public void createGameID(GameData g) {

    }

    public void createGame(GameData g) {

    }

    public UserData getUser(String username) {
        return null;
    }

    public AuthData getAuth(String authToken) {
        return null;
    }

    public Collection<GameData> getGames() {
        return new HashSet<>();
    }

    public GameData getGame(String gameName) {
        return null;
    }

    public void deleteAuth(String username) {
        return;
    }

    public void joinGame(String username, String playerColor, int gameID) {
        return;
    }

    public void clearUsers() {
        return;
    }

    public void clearGames() {
        return;
    }

    public void clearAuthTokens() {
        return;
    }
}
