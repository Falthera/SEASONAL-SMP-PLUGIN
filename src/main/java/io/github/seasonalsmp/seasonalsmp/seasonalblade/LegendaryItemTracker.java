package io.github.seasonalsmp.seasonalsmp.seasonalblade;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import org.bukkit.configuration.ConfigurationSection;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.*;

public class LegendaryItemTracker {

    private final SeasonalSMP plugin;
    private final Gson gson;
    private final File dataFile;
    private final Map<String, Boolean> craftedItems;

    private static final String DATA_FILENAME = "legendary-items.json";
    private static final List<String> TRACKED_ITEMS = List.of(
        "spring_sword",
        "summer_sword",
        "autumn_sword",
        "winter_sword",
        "seasonal_blade"
    );

    public LegendaryItemTracker(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        File dataDir = new File(plugin.getDataFolder(), "data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        this.dataFile = new File(dataDir, DATA_FILENAME);
        this.craftedItems = new HashMap<>();
        load();
    }

    public boolean hasBeenCrafted(String itemKey) {
        return craftedItems.getOrDefault(itemKey, false);
    }

    public void markCrafted(String itemKey) {
        craftedItems.put(itemKey, true);
        save();
    }

    public boolean canCraft(String itemKey) {
        return !hasBeenCrafted(itemKey);
    }

    public Set<String> getCraftedItems() {
        return Collections.unmodifiableSet(craftedItems.entrySet().stream()
            .filter(Map.Entry::getValue)
            .map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toSet()));
    }

    private void load() {
        if (!dataFile.exists()) {
            for (String key : TRACKED_ITEMS) {
                craftedItems.put(key, false);
            }
            save();
            return;
        }
        try (Reader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, Boolean>>(){}.getType();
            Map<String, Boolean> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                craftedItems.clear();
                for (String key : TRACKED_ITEMS) {
                    craftedItems.put(key, loaded.getOrDefault(key, false));
                }
            } else {
                for (String key : TRACKED_ITEMS) {
                    craftedItems.put(key, false);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load legendary item tracker: " + e.getMessage());
            for (String key : TRACKED_ITEMS) {
                craftedItems.put(key, false);
            }
        }
    }

    private void save() {
        try (Writer writer = new FileWriter(dataFile)) {
            gson.toJson(craftedItems, writer);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save legendary item tracker: " + e.getMessage());
        }
    }
}
