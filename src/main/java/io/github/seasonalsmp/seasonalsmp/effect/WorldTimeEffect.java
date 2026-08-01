package io.github.seasonalsmp.seasonalsmp.effect;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import org.bukkit.World;
import org.bukkit.GameRule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class WorldTimeEffect implements SeasonEffectsManager.SeasonEffect {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;
    private org.bukkit.scheduler.BukkitTask timeTask;
    private final Map<String, Boolean> previousDaylightCycle;

    WorldTimeEffect(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.previousDaylightCycle = new ConcurrentHashMap<>();
    }

    @Override
    public void apply(Season season) {
        if (timeTask != null && !timeTask.isCancelled()) {
            timeTask.cancel();
        }
        if (!configManager.getBoolean("world.apply-gamerule-changes")) {
            return;
        }
        for (World world : plugin.getServer().getWorlds()) {
            if (!configManager.getStringList("world.apply-effects-to").contains(world.getName())) {
                continue;
            }
            Boolean current = world.getGameRule(GameRule.DO_DAYLIGHT_CYCLE);
            if (current != null) {
                previousDaylightCycle.put(world.getName(), current);
            }
            double multiplier = 1.0;
            switch (season) {
                case SUMMER -> multiplier = configManager.getDouble("world-transformation.day-length-multiplier.summer", 1.3);
                case WINTER -> multiplier = configManager.getDouble("world-transformation.night-length-multiplier.winter", 1.3);
                default -> multiplier = 1.0;
            }
            if (multiplier > 1.0) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            } else if (multiplier < 1.0) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            }
        }
        timeTask = new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isEnabled()) {
                    return;
                }
                for (World world : plugin.getServer().getWorlds()) {
                    if (!configManager.getStringList("world.apply-effects-to").contains(world.getName())) {
                        continue;
                    }
                    if (!Boolean.FALSE.equals(world.getGameRule(GameRule.DO_DAYLIGHT_CYCLE))) {
                        continue;
                    }
                    long time = world.getTime();
                    long dayTicks = 24000L;
                    double multiplier = 1.0;
                    switch (season) {
                        case SUMMER -> multiplier = configManager.getDouble("world-transformation.day-length-multiplier.summer", 1.3);
                        case WINTER -> multiplier = configManager.getDouble("world-transformation.night-length-multiplier.winter", 1.3);
                        default -> multiplier = 1.0;
                    }
                    long increment = Math.max(1L, (long) Math.ceil(1.0 * multiplier));
                    long newTime = (time + increment) % dayTicks;
                    world.setTime(newTime);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @Override
    public void remove(Season season) {
        if (timeTask != null && !timeTask.isCancelled()) {
            timeTask.cancel();
            timeTask = null;
        }
        for (World world : plugin.getServer().getWorlds()) {
            if (!configManager.getStringList("world.apply-effects-to").contains(world.getName())) {
                continue;
            }
            Boolean previous = previousDaylightCycle.get(world.getName());
            if (previous != null) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, previous);
            } else {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            }
        }
        previousDaylightCycle.clear();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
