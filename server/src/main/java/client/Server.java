package client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dataAccess.*;
import model.*;
import service.UserService;
import spark.*;

import java.util.Collection;
import java.util.HashSet;

public class Server {
    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private final UserService service = new UserService(new MySqlDataAccess());

    public Server() {
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.delete("/db", this::clearApplication);
        Spark.post("/user", this::registerUser);
        Spark.post("/session", this::loginUser);
        Spark.delete("/session", this::logoutUser);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);

        Spark.awaitInitialization();
        return Spark.port();
    }

    private Object clearApplication(Request req, Response res) {
        try {
            service.clearApplication();
            res.status(200);
            return "";
        } catch (Exception error) {
            res.status(500);
            res.type("application/json");
            return gson.toJson(new JsonMessage(String.format("Error: %s", error)));
        }
    }

    private Object registerUser(Request req, Response res) {
        JsonObject jsonObject = gson.fromJson(req.body(), JsonObject.class);
        res.type("application/json");

        try {
            if (!jsonObject.has("username") || !jsonObject.has("password") || !jsonObject.has("email")) {
                res.status(400);
                return gson.toJson(new JsonMessage("Error: bad request"));
            }

            UserData newUser = gson.fromJson(jsonObject, UserData.class);
            AuthData auth = service.register(newUser);
            res.status(200);
            return gson.toJson(auth);

        } catch (UnauthorizedException u) {
            res.status(403);
            return gson.toJson(new JsonMessage("Error: already taken"));

        } catch (Exception error) {
            res.status(500);
            return gson.toJson(new JsonMessage(String.format("Error: %s", error)));
        }
    }

    private Object loginUser(Request req, Response res) {
        JsonObject jsonObject = gson.fromJson(req.body(), JsonObject.class);
        res.type("application/json");

        try {
            LoginRequest newLogin = gson.fromJson(jsonObject, LoginRequest.class);
            AuthData auth = service.login(newLogin.username(), newLogin.password());
            res.status(200);
            return gson.toJson(auth);

        } catch (UnauthorizedException | DataAccessException e) {
            res.status(401);
            return gson.toJson(new JsonMessage("Error: unauthorized"));

        } catch (Exception error) {
            res.status(500);
            return gson.toJson(new JsonMessage(String.format("Error: %s", error)));
        }
    }

    private Object logoutUser(Request req, Response res) {
        String authToken = req.headers("authorization");
        res.type("application/json");

        try {
            service.logout(authToken);
            res.status(200);
            return "";

        } catch (UnauthorizedException u) {
            res.status(401);
            return gson.toJson(new JsonMessage("Error: unauthorized"));

        } catch (Exception error) {
            res.status(500);
            return gson.toJson(new JsonMessage(String.format("Error: %s", error)));
        }
    }

    private Object listGames(Request req, Response res) {
        String authToken = req.headers("authorization");
        res.type("application/json");

        try {
            Collection<GameData> gameList = service.listGames(authToken);
            res.status(200);
            Collection<GameSummary> gameSummaryCollection = new HashSet<>();
            for (GameData game : gameList) {
                gameSummaryCollection.add(new GameSummary(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName()));
            }
            return gson.toJson(new GameList(gameSummaryCollection));

        } catch (UnauthorizedException u) {
            res.status(401);
            return gson.toJson(new JsonMessage("Error: unauthorized"));

        } catch (Exception error) {
            res.status(500);
            return gson.toJson(new JsonMessage(String.format("Error: %s", error)));
        }
    }

    private Object createGame(Request req, Response res) {
        String authToken = req.headers("authorization");
        JsonObject jsonObject = gson.fromJson(req.body(), JsonObject.class);
        res.type("application/json");

        try {
            if (!jsonObject.has("gameName")) {
                res.status(400);
                return gson.toJson(new JsonMessage("Error: bad request"));
            }

            GameName name = gson.fromJson(jsonObject, GameName.class);
            int gameID = service.createGame(authToken, name.gameName());
            res.status(200);
            return gson.toJson(new GameID(gameID));

        } catch (UnauthorizedException u) {
            res.status(401);
            return gson.toJson(new JsonMessage("Error: unauthorized"));

        } catch (Exception error) {
            res.status(500);
            return gson.toJson(new JsonMessage(String.format("Error: %s", error)));
        }
    }

    private Object joinGame(Request req, Response res) {
        String authToken = req.headers("authorization");
        JsonObject jsonObject = gson.fromJson(req.body(), JsonObject.class);
        res.type("application/json");

        try {
            if (!jsonObject.has("gameID")) {
                res.status(400);
                return gson.toJson(new JsonMessage("Error: bad request"));
            }

            if (jsonObject.has("playerColor")) {
                JoinInformation join = gson.fromJson(jsonObject, JoinInformation.class);
                service.joinGame(authToken, join.playerColor(), join.gameID());
            } else {
                // Add player as an observer
                GameID join = gson.fromJson(jsonObject, GameID.class);
                service.joinGame(authToken, null, join.gameID());
            }

            res.status(200);
            return "";

        } catch (InvalidGameID i) {
            res.status(400);
            return gson.toJson(new JsonMessage("Error: bad request"));

        } catch (UnauthorizedException u) {
            res.status(401);
            return gson.toJson(new JsonMessage("Error: unauthorized"));

        } catch (DataAccessException d) {
            res.status(403);
            return gson.toJson(new JsonMessage("Error: already taken"));

        } catch (Exception error) {
            res.status(500);
            return gson.toJson(new JsonMessage(String.format("Error: %s", error)));
        }
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
