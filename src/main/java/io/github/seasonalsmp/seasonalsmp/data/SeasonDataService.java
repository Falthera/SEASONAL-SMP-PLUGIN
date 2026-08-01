package io.github.seasonalsmp.seasonalsmp.data;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.season.Season;

import java.util.*;

public class SeasonDataService {

    private final SeasonalSMP plugin;
    private final DataStorage storage;
    private boolean initialized;

    public SeasonDataService(SeasonalSMP plugin, DataStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
        this.initialized = false;
    }

    public void initialize() {
        storage.initialize();
        this.initialized = true;
    }

    public Season loadCurrentSeason() {
        if (!initialized) {
            return null;
        }
        return storage.getSavedSeason();
    }

    public void saveSeason(Season season) {
        if (!initialized) {
            return;
        }
        storage.setSavedSeason(season);
        storage.persistSeason();
    }

    public void shutdown() {
        storage.shutdown();
    }
}
