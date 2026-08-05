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

public class AdminManager {

    private final SeasonalSMP plugin;
    private final Gson gson;
    private final File dataFile;
    private final Set<UUID> admins;

    public AdminManager(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        File dataDir = new File(plugin.getDataFolder(), "data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        this.dataFile = new File(dataDir, "admins.json");
        this.admins = ConcurrentHashMap.newKeySet();
        load();
    }

    public void addAdmin(Player player) {
        if (player == null) {
            return;
        }
        admins.add(player.getUniqueId());
        save();
    }

    public void removeAdmin(Player player) {
        if (player == null) {
            return;
        }
        admins.remove(player.getUniqueId());
        save();
    }

    public boolean isAdmin(Player player) {
        if (player == null) {
            return false;
        }
        return admins.contains(player.getUniqueId());
    }

    public Set<UUID> getAdmins() {
        return Collections.unmodifiableSet(admins);
    }

    private void load() {
        if (!dataFile.exists()) {
            return;
        }
        try (Reader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<List<String>>(){}.getType();
            List<String> loaded = plugin.getGson().fromJson(reader, type);
            if (loaded != null) {
                admins.clear();
                for (String uuidString : loaded) {
                    try {
                        admins.add(UUID.fromString(uuidString));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load admins: " + e.getMessage());
        }
    }

    private void save() {
        try (Writer writer = new FileWriter(dataFile)) {
            List<String> serializable = new ArrayList<>();
            for (UUID uuid : admins) {
                serializable.add(uuid.toString());
            }
            gson.toJson(serializable, writer);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save admins: " + e.getMessage());
        }
    }
}
