package io.github.seasonalsmp.seasonalsmp.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.event.relic.RelicType;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import org.bukkit.entity.Player;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
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
    private final File relicDataFile;
    private final File graceDataFile;
    private final Map<UUID, BoundType> boundCache;
    private final Set<UUID> changedBounds;
    private final Map<UUID, Set<RelicType>> relicCache;
    private final Set<UUID> bloodbornCache;
    private final Set<UUID> changedRelics;
    private volatile long graceEndTime;
    private Season savedSeason;
    private int savedDay;

    private static final String BOUND_DATA_FILENAME = "bounds.json";
    private static final String SEASON_DATA_FILENAME = "season.json";
    private static final String RELIC_DATA_FILENAME = "relics.json";
    private static final String GRACE_DATA_FILENAME = "grace.json";

    public DataStorage(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataDir = new File(plugin.getDataFolder(), "data");
        this.boundDataFile = new File(dataDir, BOUND_DATA_FILENAME);
        this.seasonDataFile = new File(dataDir, SEASON_DATA_FILENAME);
        this.relicDataFile = new File(dataDir, RELIC_DATA_FILENAME);
        this.graceDataFile = new File(dataDir, GRACE_DATA_FILENAME);
        this.boundCache = new HashMap<>();
        this.changedBounds = new HashSet<>();
        this.relicCache = new HashMap<>();
        this.bloodbornCache = new HashSet<>();
        this.changedRelics = new HashSet<>();
        this.graceEndTime = 0L;
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
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(boundDataFile), StandardCharsets.UTF_8)) {
                    gson.toJson(Collections.emptyMap(), writer);
                }
            }
            if (!seasonDataFile.exists()) {
                seasonDataFile.createNewFile();
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(seasonDataFile), StandardCharsets.UTF_8)) {
                    Map<String, Object> seedData = new LinkedHashMap<>();
                    seedData.put("season", null);
                    seedData.put("day", 1);
                    gson.toJson(seedData, writer);
                }
            }
            if (!relicDataFile.exists()) {
                relicDataFile.createNewFile();
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(relicDataFile), StandardCharsets.UTF_8)) {
                    Map<String, Object> seedData = new LinkedHashMap<>();
                    seedData.put("relics", Collections.emptyMap());
                    seedData.put("bloodborn", Collections.emptyList());
                    gson.toJson(seedData, writer);
                }
            }
            if (!graceDataFile.exists()) {
                graceDataFile.createNewFile();
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(graceDataFile), StandardCharsets.UTF_8)) {
                    Map<String, Object> seedData = new LinkedHashMap<>();
                    seedData.put("graceEndTime", 0L);
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
        loadRelicsFromDisk();
        loadGraceFromDisk();
        validateAndMigrate();
    }

    private void validateAndMigrate() {
        validateBounds();
        validateRelics();
        validateSeason();
    }

    private void validateBounds() {
        synchronized (boundCache) {
            Set<UUID> invalid = new HashSet<>();
            for (Map.Entry<UUID, BoundType> entry : boundCache.entrySet()) {
                if (entry.getValue() == null || !isValidBoundType(entry.getValue())) {
                    invalid.add(entry.getKey());
                }
            }
            for (UUID uuid : invalid) {
                boundCache.remove(uuid);
                changedBounds.add(uuid);
            }
            if (!invalid.isEmpty()) {
                plugin.getLogger().warning("Removed " + invalid.size() + " invalid bound entries");
            }
        }
    }

    private void validateRelics() {
        synchronized (relicCache) {
            Set<UUID> invalid = new HashSet<>();
            for (Map.Entry<UUID, Set<RelicType>> entry : relicCache.entrySet()) {
                Set<RelicType> valid = new HashSet<>();
                for (RelicType relic : entry.getValue()) {
                    if (isValidRelicType(relic)) {
                        valid.add(relic);
                    }
                }
                if (valid.isEmpty()) {
                    invalid.add(entry.getKey());
                } else {
                    entry.setValue(valid);
                }
            }
            for (UUID uuid : invalid) {
                relicCache.remove(uuid);
            }
            if (!invalid.isEmpty()) {
                plugin.getLogger().warning("Removed " + invalid.size() + " invalid relic entries");
            }
        }
    }

    private void validateSeason() {
        if (savedSeason == null || !isValidSeason(savedSeason)) {
            this.savedSeason = null;
            this.savedDay = 1;
            plugin.getLogger().warning("Reset invalid saved season data");
        }
    }

    private boolean isValidBoundType(BoundType type) {
        try {
            return BoundType.valueOf(type.name()) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isValidRelicType(RelicType type) {
        try {
            return RelicType.valueOf(type.name()) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isValidSeason(Season season) {
        try {
            return Season.valueOf(season.name()) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
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
                Number dayNumber = (Number) data.get("day");
                this.savedDay = dayNumber != null ? dayNumber.intValue() : 1;
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load season data", e);
        }
    }

    public void loadRelicsFromDisk() {
        synchronized (relicCache) {
            relicCache.clear();
            bloodbornCache.clear();
            if (!relicDataFile.exists()) {
                return;
            }
            try (Reader reader = new FileReader(relicDataFile)) {
                Type type = new TypeToken<Map<String, Object>>() {
                }.getType();
                Map<String, Object> data = gson.fromJson(reader, type);
                if (data != null) {
                    Object relicsObj = data.get("relics");
                    if (relicsObj instanceof Map<?, ?> rawRelics) {
                        for (Map.Entry<?, ?> entry : rawRelics.entrySet()) {
                            try {
                                UUID uuid = UUID.fromString(entry.getKey().toString());
                                Object value = entry.getValue();
                                if (value instanceof List<?> relicList) {
                                    Set<RelicType> relics = new HashSet<>();
                                    for (Object o : relicList) {
                                        if (o instanceof String s) {
                                            try {
                                                relics.add(RelicType.valueOf(s));
                                            } catch (IllegalArgumentException ignored) {
                                            }
                                        }
                                    }
                                    relicCache.put(uuid, relics);
                                }
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().warning("Skipping invalid relic UUID: " + entry.getKey());
                            }
                        }
                    }
                    Object bloodbornObj = data.get("bloodborn");
                    if (bloodbornObj instanceof List<?> bloodbornList) {
                        for (Object o : bloodbornList) {
                            if (o instanceof String s) {
                                try {
                                    bloodbornCache.add(UUID.fromString(s));
                                } catch (IllegalArgumentException ignored) {
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load relic data", e);
            }
        }
    }

    public void loadGraceFromDisk() {
        if (!graceDataFile.exists()) {
            this.graceEndTime = 0L;
            return;
        }
        try (Reader reader = new FileReader(graceDataFile)) {
            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();
            Map<String, Object> data = gson.fromJson(reader, type);
            if (data != null) {
                Object graceEnd = data.get("graceEndTime");
                if (graceEnd instanceof Number n) {
                    this.graceEndTime = n.longValue();
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load grace data", e);
        }
    }

    public void persistGrace() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(graceDataFile), StandardCharsets.UTF_8)) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("graceEndTime", graceEndTime);
            gson.toJson(data, writer);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to persist grace data", e);
        }
    }

    public void persistRelics() {
        synchronized (relicCache) {
            Map<String, List<String>> serializable = new LinkedHashMap<>();
            for (Map.Entry<UUID, Set<RelicType>> entry : relicCache.entrySet()) {
                List<String> relicNames = new ArrayList<>();
                for (RelicType relic : entry.getValue()) {
                    relicNames.add(relic.name());
                }
                serializable.put(entry.getKey().toString(), relicNames);
            }
            List<String> bloodbornList = new ArrayList<>();
            for (UUID uuid : bloodbornCache) {
                bloodbornList.add(uuid.toString());
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("relics", serializable);
            data.put("bloodborn", bloodbornList);
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(relicDataFile), StandardCharsets.UTF_8)) {
                gson.toJson(data, writer);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to persist relic data", e);
            }
            changedRelics.clear();
        }
    }

    public void persistBounds() {
        synchronized (boundCache) {
            Map<String, BoundType> serializable = new LinkedHashMap<>();
            for (Map.Entry<UUID, BoundType> entry : boundCache.entrySet()) {
                serializable.put(entry.getKey().toString(), entry.getValue());
            }
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(boundDataFile), StandardCharsets.UTF_8)) {
                gson.toJson(serializable, writer);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to persist bound data", e);
            }
            changedBounds.clear();
        }
    }

    public void persistSeason() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(seasonDataFile), StandardCharsets.UTF_8)) {
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
        persistRelics();
        persistGrace();
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

    public Set<RelicType> getPlayerRelics(UUID uuid) {
        if (uuid == null) {
            return Collections.emptySet();
        }
        synchronized (relicCache) {
            Set<RelicType> relics = relicCache.get(uuid);
            return relics != null ? Collections.unmodifiableSet(new HashSet<>(relics)) : Collections.emptySet();
        }
    }

    public boolean hasRelic(UUID uuid, RelicType relic) {
        if (uuid == null || relic == null) {
            return false;
        }
        synchronized (relicCache) {
            Set<RelicType> relics = relicCache.get(uuid);
            return relics != null && relics.contains(relic);
        }
    }

    public boolean hasAllRelics(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        synchronized (relicCache) {
            Set<RelicType> relics = relicCache.get(uuid);
            if (relics == null) {
                return false;
            }
            return relics.containsAll(Arrays.asList(
                RelicType.SPRING_RELIC,
                RelicType.SUMMER_RELIC,
                RelicType.AUTUMN_RELIC,
                RelicType.WINTER_RELIC
            ));
        }
    }

    public void addRelic(UUID uuid, RelicType relic) {
        if (uuid == null || relic == null) {
            return;
        }
        synchronized (relicCache) {
            relicCache.computeIfAbsent(uuid, k -> new HashSet<>()).add(relic);
            changedRelics.add(uuid);
        }
    }

    public void grantBloodborn(UUID uuid) {
        if (uuid == null) {
            return;
        }
        synchronized (relicCache) {
            bloodbornCache.add(uuid);
            changedRelics.add(uuid);
        }
    }

    public boolean hasBloodborn(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        synchronized (relicCache) {
            return bloodbornCache.contains(uuid);
        }
    }

    public void clearPlayerRelics(UUID uuid) {
        if (uuid == null) {
            return;
        }
        synchronized (relicCache) {
            relicCache.remove(uuid);
            bloodbornCache.remove(uuid);
            changedRelics.add(uuid);
        }
    }

    public long getGraceEndTime() {
        return graceEndTime;
    }

    public void setGraceEndTime(long graceEndTime) {
        this.graceEndTime = graceEndTime;
    }
}
