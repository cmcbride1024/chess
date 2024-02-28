package dataAccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.*;

import static chess.ChessGame.TeamColor.WHITE;

public class MemoryDataAccess implements DataAccess {
    private int userId = 1;
    private int gameId = 1;
    private final HashMap<Integer, UserData> users = new HashMap<>();
    private final HashMap<UserData, AuthData> authTokens = new HashMap<>();
    private final HashMap<String, Integer> gameIds = new HashMap<>();
    private final HashSet<GameData> games = new HashSet<>();

    public UserData createUser(UserData u) {
        var user = new UserData(u.username(), u.password(), u.email());
        users.put(userId++, user);

        return user;
    }

    public AuthData createAuth(UserData u) {
        String newUuid = UUID.randomUUID().toString();
        var newAuthToken = new AuthData(newUuid, u.username());
        authTokens.put(u, newAuthToken);

        return newAuthToken;
    }

    public Integer createGameId(String gameName) {
        gameIds.put(gameName, gameId++);

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
        for (AuthData auth : authTokens.values()) {
            if (auth.authToken().equals(authToken)) {
                return auth;
            }
        }

        return null;
    }

    public HashMap<UserData, AuthData> getAuths() {
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

    public void deleteAuth(String username) {
        for (Map.Entry<UserData, AuthData> entry : authTokens.entrySet()) {
            AuthData auth = entry.getValue();
            if (auth.username().equals(username)) {
                UserData user = entry.getKey();
                authTokens.remove(user);
            }
        }
    }

    public void joinGame(String username, String playerColor, int gameId) throws DataAccessException {
        if (!gameIds.containsValue(gameId)) {
            throw new DataAccessException("Game does not exist.");
        }

        for (GameData game : games) {
            if (game.gameID() == gameId) {
                switch (playerColor) {
                    case "WHITE":
                        games.add(game.changeWhiteName(username));
                        break;
                    case "BLACK":
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
