package dataAccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.*;

public class MemoryDataAccess implements DataAccess {
    private int userId = 0;
    private int gameId = 0;
    private final HashMap<Integer, UserData> users = new HashMap<>();
    private final HashMap<UserData, List<AuthData>> authTokens = new HashMap<>();
    private final HashMap<String, Integer> gameIds = new HashMap<>();
    private final HashSet<GameData> games = new HashSet<>();

    public UserData createUser(UserData userData) {
        var user = new UserData(userData.username(), userData.password(), userData.email());
        users.put(++userId, user);

        return user;
    }

    public AuthData createAuth(UserData user) {
        String newUUID = UUID.randomUUID().toString();
        List<AuthData> userAuths = authTokens.get(user);

        AuthData newAuthData = new AuthData(newUUID, user.username());
        if (userAuths == null) {
            userAuths = new ArrayList<>();
            userAuths.add(newAuthData);
            authTokens.put(user, userAuths);
        } else {
            userAuths.add(newAuthData);
        }

        return newAuthData;
    }

    public Integer createGameId(String gameName) {
        gameIds.put(gameName, ++gameId);

        return gameId;
    }

    public Integer createGame(GameData g) {
        games.add(g);

        return g.gameID();
    }

    public UserData getUser(String username) {
        for (UserData user : users.values()) {
            if (user.username().equals(username)) {
                return user;
            }
        }

        return null;
    }

    public HashMap<Integer, UserData> getUsers() {
        return users;
    }

    public AuthData getAuth(String authToken) {
        for (Map.Entry<UserData, List<AuthData>> entry : authTokens.entrySet()) {
            List<AuthData> authList = entry.getValue();

            for (AuthData auth : authList) {
                if (auth.authToken().equals(authToken)) {
                    return auth;
                }
            }
        }

        return null;
    }

    public HashMap<UserData, List<AuthData>> getAuths() {
        return authTokens;
    }

    public Collection<GameData> getGames() {
        return games;
    }

    public GameData getGame(String gameName) {
        for (GameData game : games) {
            if (game.gameName().equals(gameName)) {
                return game;
            }
        }

        return null;
    }

    public void deleteAuth(AuthData authToken) {
        for (Map.Entry<UserData, List<AuthData>> entry : getAuths().entrySet()) {
            List<AuthData> authList = entry.getValue();

            for (AuthData auth : authList) {
                if (auth.equals(authToken)) {
                    if (authList.size() == 1) {
                        authTokens.remove(entry.getKey());
                    } else {
                        authList.remove(auth);
                    }
                    break;
                }
            }

        }
    }

    public void joinGame(String username, String playerColor, int gameId) throws InvalidGameID, DataAccessException {
        if (!gameIds.containsValue(gameId)) {
            throw new InvalidGameID("Game does not exist.");
        }

        for (GameData game : games) {
            if (game.gameID() == gameId && playerColor != null) {
                switch (playerColor) {
                    case "WHITE":
                        if (game.whiteUsername() != null) {
                            throw new DataAccessException("Player has already joined as white.");
                        }
                        games.add(game.changeWhiteName(username));
                        break;
                    case "BLACK":
                        if (game.blackUsername() != null) {
                            throw new DataAccessException("Player has already joined as white.");
                        }
                        games.add(game.changeBlackName(username));
                        break;
                }
                games.remove(game);
            }
        }
    }

    public void clearUsers() {
        users.clear();
        userId = 1;
    }

    public void clearGames() {
        games.clear();
        gameId = 1;
    }

    public void clearAuthTokens() {
        authTokens.clear();
    }
}
