package io.github.seasonalsmp.seasonalsmp.effect;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Random;

class WeatherEffect implements SeasonEffectsManager.SeasonEffect {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;
    private final Random random;

    WeatherEffect(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.random = new Random();
    }

    @Override
    public void apply(Season season) {
        for (World world : plugin.getServer().getWorlds()) {
            if (world.hasStorm() || world.isThundering()) {
                continue;
            }
            double chance = switch (season) {
                case SPRING -> configManager.getDouble("weather.spring.rain-chance");
                case SUMMER -> configManager.getDouble("weather.summer.clear-chance");
                case AUTUMN -> configManager.getDouble("weather.autumn.rain-chance");
                case WINTER -> configManager.getDouble("weather.winter.snow-chance");
            };
            if (random.nextDouble() < chance) {
                world.setStorm(true);
                switch (season) {
                    case WINTER -> world.setWeatherDuration(24000);
                    case SPRING -> {
                        world.setWeatherDuration(12000);
                        world.setThunderDuration(random.nextDouble() < configManager.getDouble("weather.spring.thunder-chance") ? 200 : 0);
                    }
                    case AUTUMN -> {
                        world.setWeatherDuration(12000);
                        world.setThunderDuration(random.nextDouble() < configManager.getDouble("weather.autumn.thunder-chance") ? 200 : 0);
                    }
                    default -> world.setWeatherDuration(8000);
                }
            }
        }
    }

    @Override
    public void remove(Season season) {
    }

    @Override
    public boolean isEnabled() {
        return configManager.getBoolean("world.apply-weather-effects");
    }
}
