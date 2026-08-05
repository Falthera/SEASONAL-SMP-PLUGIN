package io.github.seasonalsmp.seasonalsmp.moderation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import org.bukkit.entity.Player;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WarnManager {

    private final SeasonalSMP plugin;
    private final Gson gson;
    private final File dataFile;
    private final Map<UUID, List<Warning>> warnings;
    private final int maxWarnings;

    public WarnManager(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.maxWarnings = plugin.getConfigManager().getInt("moderation.max-warnings", 3);
        File dataDir = new File(plugin.getDataFolder(), "data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        this.dataFile = new File(dataDir, "warnings.json");
        this.warnings = new ConcurrentHashMap<>();
        load();
    }

    public void warn(Player target, String reason) {
        if (target == null || reason == null) {
            return;
        }
        UUID uuid = target.getUniqueId();
        Warning warning = new Warning(System.currentTimeMillis(), reason);
        warnings.computeIfAbsent(uuid, k -> new ArrayList<>()).add(warning);
        save();
    }

    public int getWarningCount(UUID uuid) {
        if (uuid == null) {
            return 0;
        }
        return warnings.getOrDefault(uuid, Collections.emptyList()).size();
    }

    public List<Warning> getWarnings(UUID uuid) {
        if (uuid == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(warnings.getOrDefault(uuid, Collections.emptyList()));
    }

    public void clearWarnings(UUID uuid) {
        if (uuid == null) {
            return;
        }
        warnings.remove(uuid);
        save();
    }

    public int getMaxWarnings() {
        return maxWarnings;
    }

    private void load() {
        if (!dataFile.exists()) {
            return;
        }
        try (Reader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, List<Warning>>>(){}.getType();
            Map<String, List<Warning>> loaded = plugin.getGson().fromJson(reader, type);
            if (loaded != null) {
                warnings.clear();
                for (Map.Entry<String, List<Warning>> entry : loaded.entrySet()) {
                    try {
                        UUID uuid = UUID.fromString(entry.getKey());
                        warnings.put(uuid, new ArrayList<>(entry.getValue()));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load warnings: " + e.getMessage());
        }
    }

    private void save() {
        try (Writer writer = new FileWriter(dataFile)) {
            Map<String, List<Warning>> serializable = new LinkedHashMap<>();
            for (Map.Entry<UUID, List<Warning>> entry : warnings.entrySet()) {
                serializable.put(entry.getKey().toString(), entry.getValue());
            }
            gson.toJson(serializable, writer);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save warnings: " + e.getMessage());
        }
    }

    public static class Warning {
        private long timestamp;
        private String reason;

        public Warning(long timestamp, String reason) {
            this.timestamp = timestamp;
            this.reason = reason;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getReason() {
            return reason;
        }
    }
}
