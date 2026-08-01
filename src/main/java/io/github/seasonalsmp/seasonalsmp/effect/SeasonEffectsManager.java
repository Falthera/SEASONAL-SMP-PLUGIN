package io.github.seasonalsmp.seasonalsmp.effect;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SeasonEffectsManager {

    public enum EffectType {
        PARTICLES,
        WEATHER,
        AUDIO,
        BLOCK_STATE,
        MOB_BEHAVIOR,
        WORLD_TIME,
        FOG
    }

    public interface SeasonEffect {
        void apply(Season season);
        void remove(Season season);
        boolean isEnabled();
    }

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;
    private final Map<EffectType, SeasonEffect> effects;
    private BukkitTask effectTask;
    private boolean initialized;

    public SeasonEffectsManager(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.effects = new EnumMap<>(EffectType.class);
        this.initialized = false;
        registerDefaultEffects();
    }

    private void registerDefaultEffects() {
        effects.put(EffectType.PARTICLES, new AmbientParticleEffect(plugin));
        effects.put(EffectType.WEATHER, new WeatherEffect(plugin));
        effects.put(EffectType.AUDIO, new AudioEffect(plugin));
        effects.put(EffectType.BLOCK_STATE, new BlockStateEffect(plugin));
        effects.put(EffectType.MOB_BEHAVIOR, new MobBehaviorEffect(plugin));
        effects.put(EffectType.WORLD_TIME, new WorldTimeEffect(plugin));
    }

    public void initialize() {
        if (initialized) {
            return;
        }
        long intervalTicks = 10L;
        effectTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isEnabled()) {
                    return;
                }
                Season current = plugin.getSeasonManager().getCurrentSeason();
                if (current == null) {
                    return;
                }
                for (SeasonEffect effect : effects.values()) {
                    if (effect.isEnabled()) {
                        effect.apply(current);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, intervalTicks);
        this.initialized = true;
    }

    public void shutdown() {
        if (effectTask != null && !effectTask.isCancelled()) {
            effectTask.cancel();
        }
        Season current = plugin.getSeasonManager().getCurrentSeason();
        if (current != null) {
            for (SeasonEffect effect : effects.values()) {
                effect.remove(current);
            }
        }
        this.initialized = false;
    }

    public void applySeasonChange(Season newSeason) {
        if (newSeason == null) {
            return;
        }
        for (SeasonEffect effect : effects.values()) {
            effect.remove(newSeason);
            if (effect.isEnabled()) {
                effect.apply(newSeason);
            }
        }
    }

    public boolean isInitialized() {
        return initialized;
    }
}
