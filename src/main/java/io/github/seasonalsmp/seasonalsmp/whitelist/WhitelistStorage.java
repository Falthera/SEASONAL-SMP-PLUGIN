package io.github.seasonalsmp.seasonalsmp.whitelist;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WhitelistStorage {

    private final SeasonalSMP plugin;
    private final String jdbcUrl;

    public WhitelistStorage(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.jdbcUrl = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/whitelist.db";
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {
            String sql = """
                CREATE TABLE IF NOT EXISTS whitelist (
                    discord_id TEXT PRIMARY KEY,
                    username TEXT NOT NULL,
                    uuid TEXT NOT NULL UNIQUE,
                    whitelisted_at TEXT NOT NULL
                )
                """;
            statement.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize whitelist database: " + e.getMessage());
        }
    }

    public boolean addPlayer(WhitelistEntry entry) {
        String sql = "INSERT OR REPLACE INTO whitelist (discord_id, username, uuid, whitelisted_at) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entry.getDiscordId());
            statement.setString(2, entry.getUsername());
            statement.setString(3, entry.getUuid());
            statement.setString(4, entry.getWhitelistedAt().toString());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to add player to whitelist: " + e.getMessage());
            return false;
        }
    }

    public boolean removePlayerByUuid(String uuid) {
        String sql = "DELETE FROM whitelist WHERE uuid = ?";
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to remove player from whitelist: " + e.getMessage());
            return false;
        }
    }

    public WhitelistEntry findByUuid(String uuid) {
        String sql = "SELECT * FROM whitelist WHERE uuid = ?";
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return mapResultSetToEntry(resultSet);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to lookup player by UUID: " + e.getMessage());
        }
        return null;
    }

    public WhitelistEntry findByUsername(String username) {
        String sql = "SELECT * FROM whitelist WHERE username = ?";
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return mapResultSetToEntry(resultSet);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to lookup player by username: " + e.getMessage());
        }
        return null;
    }

    public WhitelistEntry findByDiscordId(String discordId) {
        String sql = "SELECT * FROM whitelist WHERE discord_id = ?";
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, discordId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return mapResultSetToEntry(resultSet);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to lookup player by Discord ID: " + e.getMessage());
        }
        return null;
    }

    public List<WhitelistEntry> getAllEntries() {
        List<WhitelistEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM whitelist";
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                entries.add(mapResultSetToEntry(resultSet));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to retrieve whitelist entries: " + e.getMessage());
        }
        return entries;
    }

    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM whitelist";
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get whitelist count: " + e.getMessage());
        }
        return 0;
    }

    private WhitelistEntry mapResultSetToEntry(ResultSet resultSet) throws SQLException {
        String discordId = resultSet.getString("discord_id");
        String username = resultSet.getString("username");
        String uuid = resultSet.getString("uuid");
        String whitelistedAtStr = resultSet.getString("whitelisted_at");
        LocalDateTime whitelistedAt = LocalDateTime.parse(whitelistedAtStr);
        return new WhitelistEntry(discordId, username, uuid, whitelistedAt);
    }
}
