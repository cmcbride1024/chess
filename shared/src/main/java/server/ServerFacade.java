package server;

import com.google.gson.Gson;
import exception.ResponseException;
import model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String url) {
        this.serverUrl = url;
    }

    public AuthData login(LoginRequest loginRequest) throws ResponseException {
        var path = "/session";
        return makeRequest("POST", path, null, loginRequest, AuthData.class);
    }

    public AuthData register(UserData userData) throws ResponseException {
        var path = "/user";
        return makeRequest("POST", path, null, userData, AuthData.class);
    }

    public GameID createGame(String gameName, String authToken) throws ResponseException {
        var path = "/game";
        return makeRequest("POST", path, authToken, gameName, GameID.class);
    }

    public GameList listGames(String authToken) throws ResponseException {
        var path = "/game";
        return makeRequest("GET", path, authToken, null, GameList.class);
    }

    public void joinGame(JoinInformation joinInformation, String authToken) throws ResponseException {
        var path = "/game";
        makeRequest("POST", path, authToken, joinInformation, null);
    }

    public void logout(String authToken) throws ResponseException {
        var path = "/session";
        makeRequest("DELETE", path, authToken, null, null);
    }

    private <T> T makeRequest(String method, String path, String authToken, Object request, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeHeader(authToken, http);
            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private void writeHeader(String authToken, HttpURLConnection http) {
        if (authToken != null) {
            http.addRequestProperty("authorization", authToken);
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            throw new ResponseException(status, "failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
