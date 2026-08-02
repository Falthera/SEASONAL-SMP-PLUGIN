package io.github.seasonalsmp.seasonalsmp.season;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.data.SeasonDataService;

import java.util.Objects;

public class SeasonManager {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;
    private final SeasonDataService dataService;
    private Season currentSeason;
    private long seasonStartTime;

    public SeasonManager(SeasonalSMP plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.configManager = plugin.getConfigManager();
        this.dataService = new SeasonDataService(plugin, new io.github.seasonalsmp.seasonalsmp.data.DataStorage(plugin));
        this.currentSeason = Season.fromString(configManager.getString("season.start-season", "SPRING"));
        if (currentSeason == null) {
            currentSeason = Season.SPRING;
        }
        this.seasonStartTime = System.currentTimeMillis();
    }

    public void initialize() {
        dataService.initialize();
        Season saved = dataService.loadCurrentSeason();
        if (saved != null) {
            currentSeason = saved;
        }
        this.seasonStartTime = System.currentTimeMillis();
        plugin.getLogger().info("Season system initialized: " + currentSeason);
    }

    public boolean advanceSeason() {
        Season previous = currentSeason;
        currentSeason = currentSeason.getNext();
        this.seasonStartTime = System.currentTimeMillis();
        dataService.saveSeason(currentSeason);
        plugin.getLogger().info("Season changed from " + previous + " to " + currentSeason);
        return true;
    }

    public Season getCurrentSeason() {
        return currentSeason;
    }

    public double getSeasonProgress() {
        long durationSeconds = configManager.getLong("season.duration-seconds", 7200);
        long elapsedSeconds = (System.currentTimeMillis() - seasonStartTime) / 1000;
        double progress = 1.0 - (elapsedSeconds / (double) durationSeconds);
        return Math.max(0.0, Math.min(1.0, progress));
    }

    public Season getNextSeason() {
        return currentSeason.getNext();
    }

    public Season getPreviousSeason() {
        return currentSeason.getPrevious();
    }

    public void setSeason(Season season) {
        if (season == null) {
            return;
        }
        Season previous = currentSeason;
        this.currentSeason = season;
        this.seasonStartTime = System.currentTimeMillis();
        dataService.saveSeason(currentSeason);
        plugin.getLogger().info("Season force-set from " + previous + " to " + currentSeason);
    }

    public void shutdown() {
        if (dataService != null) {
            dataService.shutdown();
        }
    }
}
