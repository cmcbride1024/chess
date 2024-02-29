package server;

import com.google.gson.Gson;
import dataAccess.MemoryDataAccess;
import service.UserService;
import spark.*;

public class Server {
    private final Gson gson = new Gson();
    private final UserService service = new UserService(new MemoryDataAccess());
    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", this::clearApplication);

        Spark.awaitInitialization();
        return Spark.port();
    }

    private Object clearApplication(Request req, Response res) {
        try {
            service.clearApplication();
            res.status(200);
            return "";
        } catch (Exception e) {
            res.status(500);
            res.type("application/json");
            return gson.toJson(new ResponseMessage("message", "Error: description"));
        }
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
 class ResponseMessage {
    private final String header;
    private final String body;

    public ResponseMessage(String header, String body) {
        this.header = header;
        this.body = body;
    }

    public String getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }
}


