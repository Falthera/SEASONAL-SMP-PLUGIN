package io.github.seasonalsmp.seasonalsmp.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import org.bukkit.entity.Player;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

public final class DataStorage {

    private final SeasonalSMP plugin;
    private final Gson gson;
    private final File dataDir;
    private final File boundDataFile;
    private final File seasonDataFile;
    private final Map<UUID, BoundType> boundCache;
    private final Set<UUID> changedBounds;
    private Season savedSeason;
    private int savedDay;

    private static final String BOUND_DATA_FILENAME = "bounds.json";
    private static final String SEASON_DATA_FILENAME = "season.json";

    public DataStorage(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataDir = new File(plugin.getDataFolder(), "data");
        this.boundDataFile = new File(dataDir, BOUND_DATA_FILENAME);
        this.seasonDataFile = new File(dataDir, SEASON_DATA_FILENAME);
        this.boundCache = new HashMap<>();
        this.changedBounds = new HashSet<>();
        this.savedSeason = null;
        this.savedDay = 1;
    }

    public void initialize() {
        try {
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            if (!boundDataFile.exists()) {
                boundDataFile.createNewFile();
                try (Writer writer = new FileWriter(boundDataFile)) {
                    gson.toJson(Collections.emptyMap(), writer);
                }
            }
            if (!seasonDataFile.exists()) {
                seasonDataFile.createNewFile();
                try (Writer writer = new FileWriter(seasonDataFile)) {
                    Map<String, Object> seedData = new LinkedHashMap<>();
                    seedData.put("season", null);
                    seedData.put("day", 1);
                    gson.toJson(seedData, writer);
                }
            }
            loadAllFromDisk();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize data storage", e);
        }
    }

    public void shutdown() {
        flushChanges();
    }

    public void loadAllFromDisk() {
        loadBoundsFromDisk();
        loadSeasonFromDisk();
    }

    public void loadBoundsFromDisk() {
        synchronized (boundCache) {
            boundCache.clear();
            if (!boundDataFile.exists()) {
                return;
            }
            try (Reader reader = new FileReader(boundDataFile)) {
                Type type = new TypeToken<Map<String, BoundType>>() {
                }.getType();
                Map<String, BoundType> raw = gson.fromJson(reader, type);
                if (raw != null) {
                    for (Map.Entry<String, BoundType> entry : raw.entrySet()) {
                        try {
                            UUID uuid = UUID.fromString(entry.getKey());
                            boundCache.put(uuid, entry.getValue());
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Skipping invalid bound UUID: " + entry.getKey());
                        }
                    }
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load bound data", e);
            }
        }
    }

    public void loadSeasonFromDisk() {
        if (!seasonDataFile.exists()) {
            this.savedSeason = null;
            this.savedDay = 1;
            return;
        }
        try (Reader reader = new FileReader(seasonDataFile)) {
            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();
            Map<String, Object> data = gson.fromJson(reader, type);
            if (data != null) {
                String seasonName = (String) data.get("season");
                this.savedSeason = Season.fromString(seasonName);
                Integer day = (Integer) data.get("day");
                this.savedDay = day != null ? day : 1;
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load season data", e);
        }
    }

    public void persistBounds() {
        synchronized (boundCache) {
            Map<String, BoundType> serializable = new LinkedHashMap<>();
            for (Map.Entry<UUID, BoundType> entry : boundCache.entrySet()) {
                serializable.put(entry.getKey().toString(), entry.getValue());
            }
            try (Writer writer = new FileWriter(boundDataFile)) {
                gson.toJson(serializable, writer);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to persist bound data", e);
            }
            changedBounds.clear();
        }
    }

    public void persistSeason() {
        try (Writer writer = new FileWriter(seasonDataFile)) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("season", savedSeason != null ? savedSeason.name() : null);
            data.put("day", savedDay);
            gson.toJson(data, writer);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to persist season data", e);
        }
    }

    public void flushChanges() {
        persistBounds();
        persistSeason();
    }

    public BoundType getBound(UUID playerId) {
        synchronized (boundCache) {
            return boundCache.get(playerId);
        }
    }

    public void setBound(UUID playerId, BoundType boundType) {
        synchronized (boundCache) {
            boundCache.put(playerId, boundType);
            changedBounds.add(playerId);
        }
    }

    public boolean hasBound(UUID playerId) {
        synchronized (boundCache) {
            return boundCache.containsKey(playerId);
        }
    }

    public void removeBound(UUID playerId) {
        synchronized (boundCache) {
            boundCache.remove(playerId);
            changedBounds.add(playerId);
        }
    }

    public boolean isChanged(UUID playerId) {
        synchronized (boundCache) {
            return changedBounds.contains(playerId);
        }
    }

    public Season getSavedSeason() {
        return savedSeason;
    }

    public void setSavedSeason(Season season) {
        this.savedSeason = season;
    }

    public int getSavedDay() {
        return savedDay;
    }

    public void setSavedDay(int day) {
        this.savedDay = Math.max(1, day);
    }

    public void saveBoundData() {
        persistBounds();
    }

    public void cachePlayerBound(UUID playerId, BoundType boundType) {
        synchronized (boundCache) {
            if (boundType == null) {
                boundCache.remove(playerId);
            } else {
                boundCache.put(playerId, boundType);
            }
        }
    }

    public int getBoundCount() {
        synchronized (boundCache) {
            return boundCache.size();
        }
    }

    public Set<UUID> getBoundKeys() {
        synchronized (boundCache) {
            return new HashSet<>(boundCache.keySet());
        }
    }

    public Map<UUID, BoundType> getAllBounds() {
        synchronized (boundCache) {
            return Collections.unmodifiableMap(new HashMap<>(boundCache));
        }
    }
}
