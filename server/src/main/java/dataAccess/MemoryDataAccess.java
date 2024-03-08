package dataAccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;


public class MemoryDataAccess implements DataAccess {
    private int userID = 0;
    private int gameID = 0;
    private final HashMap<Integer, UserData> users = new HashMap<>();
    private final HashMap<UserData, List<AuthData>> authTokens = new HashMap<>();
    private final HashMap<String, Integer> gameIDs = new HashMap<>();
    private final HashSet<GameData> games = new HashSet<>();

    @Override
    public void createUser(UserData userData) {
        var user = new UserData(userData.username(), userData.password(), userData.email());
        getUsers().put(++userID, user);
    }

    @Override
    public AuthData createAuth(UserData userData) {
        String newUUID = UUID.randomUUID().toString();
        List<AuthData> userAuths = authTokens.get(userData);

        AuthData newAuthData = new AuthData(newUUID, userData.username());
        if (userAuths == null) {
            userAuths = new ArrayList<>();
            userAuths.add(newAuthData);
            authTokens.put(userData, userAuths);
        } else {
            userAuths.add(newAuthData);
        }

        return newAuthData;
    }

    @Override
    public Integer createGameID(String gameName) {
        gameIDs.put(gameName, ++gameID);

        return gameID;
    }

    @Override
    public void createGame(GameData gameData) {
        games.add(gameData);
    }

    @Override
    public UserData getUser(String username) {
        for (UserData user : getUsers().values()) {
            if (user.username().equals(username)) {
                return user;
            }
        }

        return null;
    }

    public HashMap<Integer, UserData> getUsers() {
        return users;
    }

    @Override
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

    @Override
    public HashMap<UserData, List<AuthData>> getAuths() {
        return authTokens;
    }

    @Override
    public Collection<GameData> getGames() {
        return games;
    }

    @Override
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

    @Override
    public void joinGame(String username, String playerColor, int gameID) throws InvalidGameID, DataAccessException {
        if (!gameIDs.containsValue(gameID)) {
            throw new InvalidGameID("Game does not exist.");
        }

        for (GameData game : games) {
            if (game.gameID() == gameID && playerColor != null) {
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

    @Override
    public void clearUsers() {
        getUsers().clear();
        userID = 1;
    }

    @Override
    public void clearGames() {
        games.clear();
        gameID = 1;
    }

    @Override
    public void clearAuthTokens() {
        authTokens.clear();
    }

    @Override
    public void clearGameIDs() { gameIDs.clear(); }

    @Override
    public String hashPassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }

    @Override
    public boolean passwordsMatch(String loginPassword, String storedPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(loginPassword, storedPassword);
    }
}
