package io.github.seasonalsmp.discordbot;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class WhitelistAPIClient {
    private final String baseUrl;
    private final String apiKey;
    private final Gson gson;

    public WhitelistAPIClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.gson = new Gson();
    }

    public ApiResponse addPlayer(String discordId, String username) {
        JsonObject payload = new JsonObject();
        payload.addProperty("discordId", discordId);
        payload.addProperty("username", username);
        return post("/api/whitelist/add", payload.toString());
    }

    public ApiResponse removePlayer(String uuid) {
        JsonObject payload = new JsonObject();
        payload.addProperty("uuid", uuid);
        return post("/api/whitelist/remove", payload.toString());
    }

    public ApiResponse lookupByUuid(String uuid) {
        return get("/api/whitelist/lookup?uuid=" + encode(uuid));
    }

    public ApiResponse lookupByUsername(String username) {
        return get("/api/whitelist/lookup?username=" + encode(username));
    }

    public ApiResponse getStats() {
        return get("/api/whitelist/stats");
    }

    public ApiResponse healthCheck() {
        return post("/api/health", "");
    }

    public ApiResponse reload() {
        return post("/api/reload", "");
    }

    private ApiResponse post(String path, String body) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(baseUrl + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            if (body != null && !body.isBlank()) {
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = body.getBytes(StandardCharsets.UTF_8);
                    os.write(input);
                }
            }

            int statusCode = connection.getResponseCode();
            String responseBody = readResponse(connection, statusCode);
            return new ApiResponse(statusCode, responseBody);
        } catch (Exception e) {
            return new ApiResponse(0, "{\"error\": \"Failed to connect to whitelist API: " + e.getMessage() + "\"}");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private ApiResponse get(String path) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(baseUrl + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int statusCode = connection.getResponseCode();
            String responseBody = readResponse(connection, statusCode);
            return new ApiResponse(statusCode, responseBody);
        } catch (Exception e) {
            return new ApiResponse(0, "{\"error\": \"Failed to connect to whitelist API: " + e.getMessage() + "\"}");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readResponse(HttpURLConnection connection, int statusCode) throws IOException {
        InputStream inputStream = statusCode >= 200 && statusCode < 300 ? connection.getInputStream() : connection.getErrorStream();
        if (inputStream == null) {
            return "{\"error\": \"No response body\"}";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private String encode(String value) {
        return value.replace(" ", "%20");
    }

    public static class ApiResponse {
        public final int statusCode;
        public final String body;

        public ApiResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }

        public String getMessage() {
            try {
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                return json.has("message") ? json.get("message").getAsString() : json.has("error") ? json.get("error").getAsString() : "Unknown error";
            } catch (Exception e) {
                return body;
            }
        }

        public String getUsername() {
            try {
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                return json.has("username") ? json.get("username").getAsString() : null;
            } catch (Exception e) {
                return null;
            }
        }

        public String getUuid() {
            try {
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                return json.has("uuid") ? json.get("uuid").getAsString() : null;
            } catch (Exception e) {
                return null;
            }
        }
    }
}
