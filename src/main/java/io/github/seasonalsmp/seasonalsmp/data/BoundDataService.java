package io.github.seasonalsmp.seasonalsmp.data;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import org.bukkit.entity.Player;

import java.util.*;

public class BoundDataService {

    private final SeasonalSMP plugin;
    private final DataStorage storage;
    private boolean initialized;

    public BoundDataService(SeasonalSMP plugin, DataStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
        this.initialized = false;
    }

    public void initialize() {
        storage.initialize();
        this.initialized = true;
    }

    public BoundType getBound(Player player) {
        if (!initialized || player == null) {
            return null;
        }
        return storage.getBound(player.getUniqueId());
    }

    public BoundType getBound(UUID uuid) {
        if (!initialized || uuid == null) {
            return null;
        }
        return storage.getBound(uuid);
    }

    public void setBound(Player player, BoundType bound) {
        if (!initialized || player == null || bound == null) {
            return;
        }
        storage.setBound(player.getUniqueId(), bound);
    }

    public void setBound(UUID uuid, BoundType bound) {
        if (!initialized || uuid == null || bound == null) {
            return;
        }
        storage.setBound(uuid, bound);
    }

    public boolean hasBound(Player player) {
        if (!initialized || player == null) {
            return false;
        }
        return storage.hasBound(player.getUniqueId());
    }

    public boolean hasBound(UUID uuid) {
        if (!initialized || uuid == null) {
            return false;
        }
        return storage.hasBound(uuid);
    }

    public void removeBound(Player player) {
        if (!initialized || player == null) {
            return;
        }
        storage.removeBound(player.getUniqueId());
    }

    public void removeBound(UUID uuid) {
        if (!initialized || uuid == null) {
            return;
        }
        storage.removeBound(uuid);
    }

    public void loadAll() {
        if (!initialized) {
            return;
        }
        storage.loadAllFromDisk();
        plugin.getLogger().info("Bound data loaded: " + storage.getBoundCount() + " entries.");
    }

    public void saveAll() {
        if (!initialized) {
            return;
        }
        storage.flushChanges();
        plugin.getLogger().info("Bound data saved.");
    }

    public void shutdown() {
        saveAll();
        storage.shutdown();
    }

    public Set<UUID> getAllBoundPlayers() {
        if (!initialized) {
            return Collections.emptySet();
        }
        return storage.getBoundKeys();
    }

    public Map<UUID, BoundType> getAllBounds() {
        if (!initialized) {
            return Collections.emptyMap();
        }
        return storage.getAllBounds();
    }
}
