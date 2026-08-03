package io.github.seasonalsmp.seasonalsmp.whitelist;

import com.google.gson.Gson;
import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

public class WhitelistAPIServer {

    private final SeasonalSMP plugin;
    private final WhitelistManager whitelistManager;
    private final Gson gson;
    private final String apiKey;
    private final int port;
    private com.sun.net.httpserver.HttpServer server;

    public WhitelistAPIServer(SeasonalSMP plugin, WhitelistManager whitelistManager) {
        this.plugin = plugin;
        this.whitelistManager = whitelistManager;
        this.gson = new Gson();
        this.apiKey = plugin.getConfigManager().getString("whitelist.api-key", "changeme");
        this.port = plugin.getConfigManager().getInt("whitelist.api-port", 8080);
    }

    public void start() {
        if (!plugin.getConfigManager().getBoolean("whitelist.enabled", true)) {
            plugin.getLogger().info("Whitelist API server is disabled in config.");
            return;
        }
        try {
            server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(port), 0);
            server.setExecutor(Executors.newCachedThreadPool());

            server.createContext("/api/health", this::handleHealth);
            server.createContext("/api/whitelist/add", this::handleAdd);
            server.createContext("/api/whitelist/remove", this::handleRemove);
            server.createContext("/api/whitelist/lookup", this::handleLookup);
            server.createContext("/api/whitelist/stats", this::handleStats);
            server.createContext("/api/reload", this::handleReload);

            server.start();
            plugin.getLogger().info("Whitelist API server started on port " + port);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to start whitelist API server: " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            plugin.getLogger().info("Whitelist API server stopped.");
        }
    }

    private void handleHealth(com.sun.net.httpserver.HttpExchange exchange) {
        if (!isGet(exchange) && !isPost(exchange)) {
            sendJson(exchange, 405, Map.of("error", "Method not allowed"));
            return;
        }
        if (!authenticate(exchange)) {
            sendJson(exchange, 401, Map.of("error", "Unauthorized"));
            return;
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("version", plugin.getDescription().getVersion());
        response.put("timestamp", System.currentTimeMillis());
        sendJson(exchange, 200, response);
    }

    private void handleAdd(com.sun.net.httpserver.HttpExchange exchange) {
        if (!isPost(exchange)) {
            sendJson(exchange, 405, Map.of("error", "Method not allowed"));
            return;
        }
        if (!authenticate(exchange)) {
            sendJson(exchange, 401, Map.of("error", "Unauthorized"));
            return;
        }

        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> request = gson.fromJson(body, Map.class);
            if (request == null || !request.containsKey("discordId") || !request.containsKey("username")) {
                sendJson(exchange, 400, Map.of("error", "Invalid request body. Required fields: discordId, username"));
                return;
            }

            String discordId = String.valueOf(request.get("discordId"));
            String username = String.valueOf(request.get("username"));

            if (discordId.isBlank() || username.isBlank()) {
                sendJson(exchange, 400, Map.of("error", "discordId and username must not be blank"));
                return;
            }

            whitelistManager.addPlayer(discordId, username).thenAccept(result -> {
                if (exchange.getResponseCode() != -1) {
                    return;
                }
                Map<String, Object> response = new LinkedHashMap<>();
                if (result.success) {
                    response.put("success", true);
                    response.put("message", result.message);
                    response.put("username", result.username);
                    response.put("uuid", result.uuid);
                    sendJson(exchange, 200, response);
                } else {
                    response.put("success", false);
                    response.put("message", result.message);
                    sendJson(exchange, 400, response);
                }
            }).exceptionally(ex -> {
                if (exchange.getResponseCode() != -1) {
                    return null;
                }
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Whitelist add failed", ex);
                sendJson(exchange, 500, Map.of("error", "Internal server error"));
                return null;
            });

        } catch (Exception e) {
            sendJson(exchange, 500, Map.of("error", "Internal server error"));
        }
    }

    private void handleRemove(com.sun.net.httpserver.HttpExchange exchange) {
        if (!isPost(exchange)) {
            sendJson(exchange, 405, Map.of("error", "Method not allowed"));
            return;
        }
        if (!authenticate(exchange)) {
            sendJson(exchange, 401, Map.of("error", "Unauthorized"));
            return;
        }

        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> request = gson.fromJson(body, Map.class);
            if (request == null || !request.containsKey("uuid")) {
                sendJson(exchange, 400, Map.of("error", "Invalid request body. Required field: uuid"));
                return;
            }

            String uuid = String.valueOf(request.get("uuid"));
            if (uuid.isBlank()) {
                sendJson(exchange, 400, Map.of("error", "uuid must not be blank"));
                return;
            }

            whitelistManager.removePlayer(uuid).thenAccept(result -> {
                if (exchange.getResponseCode() != -1) {
                    return;
                }
                Map<String, Object> response = new LinkedHashMap<>();
                if (result.success) {
                    response.put("success", true);
                    response.put("message", result.message);
                    sendJson(exchange, 200, response);
                } else {
                    response.put("success", false);
                    response.put("message", result.message);
                    sendJson(exchange, 400, response);
                }
            }).exceptionally(ex -> {
                if (exchange.getResponseCode() != -1) {
                    return null;
                }
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Whitelist remove failed", ex);
                sendJson(exchange, 500, Map.of("error", "Internal server error"));
                return null;
            });

        } catch (Exception e) {
            sendJson(exchange, 500, Map.of("error", "Internal server error"));
        }
    }

    private void handleLookup(com.sun.net.httpserver.HttpExchange exchange) {
        if (!isGet(exchange)) {
            sendJson(exchange, 405, Map.of("error", "Method not allowed"));
            return;
        }
        if (!authenticate(exchange)) {
            sendJson(exchange, 401, Map.of("error", "Unauthorized"));
            return;
        }

        try {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            String uuid = params.get("uuid");
            String username = params.get("username");

            if (uuid != null && !uuid.isBlank()) {
                whitelistManager.lookupByUuid(uuid).thenAccept(result -> {
                    if (exchange.getResponseCode() != -1) {
                        return;
                    }
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("found", result.found);
                    response.put("message", result.message);
                    response.put("data", result.data);
                    sendJson(exchange, result.found ? 200 : 404, response);
                }).exceptionally(ex -> {
                    if (exchange.getResponseCode() != -1) {
                        return null;
                    }
                    plugin.getLogger().log(java.util.logging.Level.SEVERE, "Whitelist lookup failed", ex);
                    sendJson(exchange, 500, Map.of("error", "Internal server error"));
                    return null;
                });
            } else if (username != null && !username.isBlank()) {
                whitelistManager.lookupByUsername(username).thenAccept(result -> {
                    if (exchange.getResponseCode() != -1) {
                        return;
                    }
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("found", result.found);
                    response.put("message", result.message);
                    response.put("data", result.data);
                    sendJson(exchange, result.found ? 200 : 404, response);
                }).exceptionally(ex -> {
                    if (exchange.getResponseCode() != -1) {
                        return null;
                    }
                    plugin.getLogger().log(java.util.logging.Level.SEVERE, "Whitelist lookup failed", ex);
                    sendJson(exchange, 500, Map.of("error", "Internal server error"));
                    return null;
                });
            } else {
                sendJson(exchange, 400, Map.of("error", "Required query parameter missing: uuid or username"));
            }

        } catch (Exception e) {
            sendJson(exchange, 500, Map.of("error", "Internal server error"));
        }
    }

    private void handleStats(com.sun.net.httpserver.HttpExchange exchange) {
        if (!isGet(exchange)) {
            sendJson(exchange, 405, Map.of("error", "Method not allowed"));
            return;
        }
        if (!authenticate(exchange)) {
            sendJson(exchange, 401, Map.of("error", "Unauthorized"));
            return;
        }

        WhitelistStats stats = whitelistManager.getStats();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalWhitelisted", stats.totalWhitelisted);
        response.put("onlineWhitelisted", stats.onlineWhitelisted);
        response.put("timestamp", System.currentTimeMillis());
        sendJson(exchange, 200, response);
    }

    private void handleReload(com.sun.net.httpserver.HttpExchange exchange) {
        if (!isPost(exchange)) {
            sendJson(exchange, 405, Map.of("error", "Method not allowed"));
            return;
        }
        if (!authenticate(exchange)) {
            sendJson(exchange, 401, Map.of("error", "Unauthorized"));
            return;
        }

        try {
            plugin.getConfigManager().reloadAll();
            whitelistManager.reload();
            sendJson(exchange, 200, Map.of("success", true, "message", "Configuration reloaded successfully."));
        } catch (Exception e) {
            sendJson(exchange, 500, Map.of("error", "Failed to reload configuration."));
        }
    }

    private boolean isPost(com.sun.net.httpserver.HttpExchange exchange) {
        return "POST".equalsIgnoreCase(exchange.getRequestMethod());
    }

    private boolean isGet(com.sun.net.httpserver.HttpExchange exchange) {
        return "GET".equalsIgnoreCase(exchange.getRequestMethod());
    }

    private boolean authenticate(com.sun.net.httpserver.HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authHeader.substring(7);
        return token.equals(apiKey);
    }

    private void sendJson(com.sun.net.httpserver.HttpExchange exchange, int statusCode, Object data) {
        try {
            String json = gson.toJson(data);
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send JSON response: " + e.getMessage());
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new LinkedHashMap<>();
        if (query == null || query.isBlank()) {
            return params;
        }
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2) {
                params.put(pair[0], URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            }
        }
        return params;
    }
}
