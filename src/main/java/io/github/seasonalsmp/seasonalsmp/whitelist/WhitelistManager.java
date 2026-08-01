package io.github.seasonalsmp.seasonalsmp.whitelist;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class WhitelistManager {

    private final SeasonalSMP plugin;
    private final WhitelistStorage storage;
    private final Gson gson;
    private final Set<String> whitelistedUuids;
    private final Map<UUID, String> playerWhitelistStatus;

    public WhitelistManager(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.storage = new WhitelistStorage(plugin);
        this.gson = new Gson();
        this.whitelistedUuids = ConcurrentHashMap.newKeySet();
        this.playerWhitelistStatus = new ConcurrentHashMap<>();
        loadWhitelist();
    }

    private void loadWhitelist() {
        whitelistedUuids.clear();
        List<WhitelistEntry> entries = storage.getAllEntries();
        for (WhitelistEntry entry : entries) {
            whitelistedUuids.add(entry.getUuid());
        }
        plugin.getLogger().info("Loaded " + entries.size() + " whitelisted players.");
    }

    public CompletableFuture<WhitelistResult> addPlayer(String discordId, String username) {
        return CompletableFuture.supplyAsync(() -> {
            if (discordId == null || discordId.isBlank()) {
                return WhitelistResult.error("Discord ID is required.");
            }
            if (username == null || username.isBlank()) {
                return WhitelistResult.error("Username is required.");
            }

            if (!isValidMinecraftUsername(username)) {
                return WhitelistResult.error("Invalid Minecraft username format.");
            }

            WhitelistEntry existingByDiscord = storage.findByDiscordId(discordId);
            if (existingByDiscord != null) {
                return WhitelistResult.error("This Discord account is already linked to a Minecraft account.");
            }

            MojangResponse mojangResponse = fetchUuidFromMojang(username);
            if (mojangResponse == null || mojangResponse.uuid == null || mojangResponse.uuid.isBlank()) {
                return WhitelistResult.error("Minecraft account not found. Please check the username.");
            }

            WhitelistEntry existingByUuid = storage.findByUuid(mojangResponse.uuid);
            if (existingByUuid != null) {
                return WhitelistResult.error("This Minecraft account is already whitelisted.");
            }

            WhitelistEntry existingByName = storage.findByUsername(mojangResponse.username);
            if (existingByName != null) {
                return WhitelistResult.error("This Minecraft username is already whitelisted.");
            }

            WhitelistEntry entry = new WhitelistEntry(discordId, mojangResponse.username, mojangResponse.uuid, LocalDateTime.now());
            boolean success = storage.addPlayer(entry);
            if (!success) {
                return WhitelistResult.error("Failed to save whitelist entry to database.");
            }

            whitelistedUuids.add(mojangResponse.uuid);
            applyWhitelist(mojangResponse.uuid);

            return WhitelistResult.success(mojangResponse.username, mojangResponse.uuid);
        });
    }

    public CompletableFuture<WhitelistResult> removePlayer(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            if (uuid == null || uuid.isBlank()) {
                return WhitelistResult.error("UUID is required.");
            }

            WhitelistEntry entry = storage.findByUuid(uuid);
            if (entry == null) {
                return WhitelistResult.error("Player not found in whitelist.");
            }

            boolean success = storage.removePlayerByUuid(uuid);
            if (!success) {
                return WhitelistResult.error("Failed to remove player from database.");
            }

            whitelistedUuids.remove(uuid);
            Bukkit.getScheduler().runTask(plugin, () -> {
                Player onlinePlayer = Bukkit.getPlayer(UUID.fromString(uuid));
                if (onlinePlayer != null) {
                    onlinePlayer.setWhitelisted(false);
                }
            });

            return WhitelistResult.success(entry.getUsername(), entry.getUuid());
        });
    }

    public CompletableFuture<WhitelistLookupResult> lookupByUuid(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            WhitelistEntry entry = storage.findByUuid(uuid);
            if (entry == null) {
                return WhitelistLookupResult.notFound("Player not found in whitelist.");
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("discordId", entry.getDiscordId());
            data.put("username", entry.getUsername());
            data.put("uuid", entry.getUuid());
            data.put("whitelistedAt", entry.getWhitelistedAt().toString());
            return WhitelistLookupResult.found(data);
        });
    }

    public CompletableFuture<WhitelistLookupResult> lookupByUsername(String username) {
        return CompletableFuture.supplyAsync(() -> {
            WhitelistEntry entry = storage.findByUsername(username);
            if (entry == null) {
                return WhitelistLookupResult.notFound("Player not found in whitelist.");
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("discordId", entry.getDiscordId());
            data.put("username", entry.getUsername());
            data.put("uuid", entry.getUuid());
            data.put("whitelistedAt", entry.getWhitelistedAt().toString());
            return WhitelistLookupResult.found(data);
        });
    }

    public List<WhitelistEntry> getAllEntriesSync() {
        return storage.getAllEntries();
    }

    public WhitelistStats getStats() {
        int total = storage.getTotalCount();
        Set<String> onlineWhitelisted = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (whitelistedUuids.contains(player.getUniqueId().toString())) {
                onlineWhitelisted.add(player.getUniqueId().toString());
            }
        }
        return new WhitelistStats(total, onlineWhitelisted.size());
    }

    public boolean isWhitelisted(UUID uuid) {
        return whitelistedUuids.contains(uuid.toString());
    }

    public void reload() {
        loadWhitelist();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isWhitelisted(player.getUniqueId())) {
                player.setWhitelisted(true);
            }
        }
    }

    private void applyWhitelist(String uuid) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                UUID playerUuid = UUID.fromString(uuid);
                Player onlinePlayer = Bukkit.getPlayer(playerUuid);
                if (onlinePlayer != null) {
                    onlinePlayer.setWhitelisted(true);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID format during whitelist apply: " + uuid);
            }
        });
    }

    private boolean isValidMinecraftUsername(String username) {
        return username.matches("^[a-zA-Z0-9_]{3,16}$");
    }

    private MojangResponse fetchUuidFromMojang(String username) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 204) {
                return null;
            }
            if (responseCode != 200) {
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                JsonObject json = gson.fromJson(response.toString(), JsonObject.class);
                if (json == null || !json.has("id") || !json.has("name")) {
                    return null;
                }
                String id = json.get("id").getAsString();
                String resolvedUsername = json.get("name").getAsString();
                return new MojangResponse(resolvedUsername, id);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to fetch UUID from Mojang for username " + username + ": " + e.getMessage());
            return null;
        }
    }

    private static class MojangResponse {
        String username;
        String uuid;

        MojangResponse(String username, String uuid) {
            this.username = username;
            this.uuid = uuid;
        }
    }
}
