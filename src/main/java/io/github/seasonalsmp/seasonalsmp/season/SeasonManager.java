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

    public SeasonManager(SeasonalSMP plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.configManager = plugin.getConfigManager();
        this.dataService = new SeasonDataService(plugin, new io.github.seasonalsmp.seasonalsmp.data.DataStorage(plugin));
        this.currentSeason = Season.fromString(configManager.getString("season.start-season", "SPRING"));
        if (currentSeason == null) {
            currentSeason = Season.SPRING;
        }
    }

    public void initialize() {
        dataService.initialize();
        Season saved = dataService.loadCurrentSeason();
        if (saved != null) {
            currentSeason = saved;
        }
        plugin.getLogger().info("Season system initialized: " + currentSeason);
    }

    public boolean advanceSeason() {
        Season previous = currentSeason;
        currentSeason = currentSeason.getNext();
        dataService.saveSeason(currentSeason);
        plugin.getLogger().info("Season changed from " + previous + " to " + currentSeason);
        return true;
    }

    public Season getCurrentSeason() {
        return currentSeason;
    }

    public double getSeasonProgress() {
        return 0.0;
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
        dataService.saveSeason(currentSeason);
        plugin.getLogger().info("Season force-set from " + previous + " to " + currentSeason);
    }

    public void shutdown() {
        if (dataService != null) {
            dataService.shutdown();
        }
    }
}
