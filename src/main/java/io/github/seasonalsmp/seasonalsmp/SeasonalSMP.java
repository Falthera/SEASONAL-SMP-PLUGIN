package io.github.seasonalsmp.seasonalsmp;

import io.github.seasonalsmp.seasonalsmp.bound.BoundManager;
import io.github.seasonalsmp.seasonalsmp.command.BoundCommand;
import io.github.seasonalsmp.seasonalsmp.command.DebugCommand;
import io.github.seasonalsmp.seasonalsmp.command.GiveSwordCommand;
import io.github.seasonalsmp.seasonalsmp.command.ReloadCommand;
import io.github.seasonalsmp.seasonalsmp.command.SeasonCommand;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.core.PluginManager;
import io.github.seasonalsmp.seasonalsmp.effect.EffectManager;
import io.github.seasonalsmp.seasonalsmp.gui.UIManager;
import io.github.seasonalsmp.seasonalsmp.listener.PlayerJoinListener;
import io.github.seasonalsmp.seasonalsmp.listener.SeasonWorldListener;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import io.github.seasonalsmp.seasonalsmp.season.SeasonManager;
import io.github.seasonalsmp.seasonalsmp.sword.SwordCombatListener;
import io.github.seasonalsmp.seasonalsmp.sword.SwordManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public final class SeasonalSMP extends JavaPlugin {

    private static SeasonalSMP instance;
    private PluginManager pluginManager;
    private ConfigManager configManager;
    private SeasonManager seasonManager;
    private BoundManager boundManager;
    private SwordManager swordManager;
    private EffectManager effectManager;
    private UIManager uiManager;
    private io.github.seasonalsmp.seasonalsmp.effect.SeasonEffectsManager seasonEffectsManager;
    private io.github.seasonalsmp.seasonalsmp.data.DataStorage dataStorage;
    private BukkitTask seasonCycleTask;
    private BukkitTask ambientEffectTask;

    @Override
    public void onEnable() {
        instance = this;
        long startTime = System.nanoTime();
        saveDefaultConfig();
        pluginManager = new PluginManager(this);
        configManager = new ConfigManager(this);
        uiManager = new UIManager(this);
        dataStorage = new io.github.seasonalsmp.seasonalsmp.data.DataStorage(this);
        seasonManager = new SeasonManager(this);
        boundManager = new BoundManager(this, dataStorage);
        swordManager = new SwordManager(this);
        effectManager = new EffectManager(this);
        configManager.loadAll();
        seasonManager.initialize();
        boundManager.loadAll();
        swordManager.initialize();
        effectManager.initialize();
        uiManager.initialize();
        seasonEffectsManager = new io.github.seasonalsmp.seasonalsmp.effect.SeasonEffectsManager(this);
        seasonEffectsManager.initialize();
        registerListeners();
        registerCommands();
        startSeasonCycle();
        startAmbientEffects();
        long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;
        getLogger().info("SeasonalSMP v" + getDescription().getVersion() + " enabled in " + elapsedMs + "ms");
        Bukkit.broadcast(Component.text(""));
        Bukkit.broadcast(Component.text("  &6&lSeasonal SMP &ehas been enabled!"));
        Bukkit.broadcast(Component.text("  &7Current season: &f" + seasonManager.getCurrentSeason().getDisplayName()));
        Bukkit.broadcast(Component.text("  &7Next change in: &f2 hours"));
        Bukkit.broadcast(Component.text(""));
    }

    @Override
    public void onDisable() {
        if (seasonCycleTask != null && !seasonCycleTask.isCancelled()) {
            seasonCycleTask.cancel();
        }
        if (ambientEffectTask != null && !ambientEffectTask.isCancelled()) {
            ambientEffectTask.cancel();
        }
        if (boundManager != null) {
            boundManager.saveAll();
        }
        if (seasonManager != null) {
            seasonManager.shutdown();
        }
        if (effectManager != null) {
            effectManager.shutdown();
        }
        if (uiManager != null) {
            uiManager.shutdown();
        }
        if (seasonEffectsManager != null) {
            seasonEffectsManager.shutdown();
        }
        instance = null;
        getLogger().info("SeasonalSMP disabled gracefully");
    }

    public static SeasonalSMP get() {
        return instance;
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SeasonWorldListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SwordCombatListener(this), this);
        io.github.seasonalsmp.seasonalsmp.event.RelicPurgeListener relicListener = new io.github.seasonalsmp.seasonalsmp.event.RelicPurgeListener(this);
        relicListener.initialize();
    }

    private void registerCommands() {
        org.bukkit.command.Command season = getCommand("season");
        if (season != null) {
            season.setExecutor(new SeasonCommand(this));
            season.setTabCompleter(new SeasonCommandTabCompleter(this));
        }
        org.bukkit.command.Command bound = getCommand("bound");
        if (bound != null) {
            bound.setExecutor(new BoundCommand(this));
            bound.setTabCompleter(new BoundCommandTabCompleter(this));
        }
        org.bukkit.command.Command givesword = getCommand("givesword");
        if (givesword != null) {
            givesword.setExecutor(new GiveSwordCommand(this));
        }
        org.bukkit.command.Command reload = getCommand("seasonreload");
        if (reload != null) {
            reload.setExecutor(new ReloadCommand(this));
        }
        org.bukkit.command.Command debug = getCommand("seasondebug");
        if (debug != null) {
            debug.setExecutor(new DebugCommand(this));
        }
        org.bukkit.command.Command ability = getCommand("ability");
        if (ability != null) {
            ability.setExecutor(new AbilityCommand(this));
        }
        org.bukkit.command.Command changebound = getCommand("changebound");
        if (changebound != null) {
            changebound.setExecutor(new ChangeBoundCommand(this));
        }
        org.bukkit.command.Command event = getCommand("event");
        if (event != null) {
            event.setExecutor(new EventCommand(this));
        }
    }

    private void startSeasonCycle() {
        long durationSeconds = configManager.getLong("season.duration-seconds", 7200);
        long cycleTicks = durationSeconds * 20L;
        seasonCycleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!seasonManager.advanceSeason()) {
                    return;
                }
                applySeasonChange();
            }
        }.runTaskTimer(this, cycleTicks, cycleTicks);
    }

    private void startAmbientEffects() {
        long intervalTicks = 10L;
        ambientEffectTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isEnabled() || seasonManager == null) {
                    return;
                }
                Season current = seasonManager.getCurrentSeason();
                if (current == null) {
                    return;
                }
            }
        }.runTaskTimer(this, 0L, intervalTicks);
    }

    private void applySeasonChange() {
        Season newSeason = seasonManager.getCurrentSeason();
        if (newSeason == null) {
            return;
        }
        for (World world : Bukkit.getWorlds()) {
            if (!configManager.getStringList("world.apply-effects-to").contains(world.getName())) {
                continue;
            }
            applySeasonGamerules(world, newSeason);
            if (configManager.getBoolean("world.apply-weather-effects")) {
                applySeasonWeather(world, newSeason);
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            applySeasonBoundEffects(player);
            uiManager.updateBossBar(player, newSeason);
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            org.bukkit.Location loc = player.getLocation();
            switch (newSeason) {
                case SPRING -> player.spawnParticle(Particle.HEART, loc, 40, 5, 5, 5, 0.2);
                case SUMMER -> player.spawnParticle(Particle.FLAME, loc, 60, 5, 3, 5, 0.4);
                case AUTUMN -> player.spawnParticle(Particle.CRIT, loc, 50, 5, 4, 5, 0.3);
                case WINTER -> player.spawnParticle(Particle.SNOWFLAKE, loc, 80, 6, 3, 6, 0.3);
            }
        }
        if (seasonEffectsManager != null) {
            seasonEffectsManager.applySeasonChange(newSeason);
        }
        String seasonName = newSeason.getDisplayName();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage("§6§lSEASON HAS CHANGED TO " + seasonName);
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }

    private void applySeasonGamerules(World world, Season season) {
        if (!configManager.getBoolean("world.apply-gamerule-changes")) {
            return;
        }
        switch (season) {
            case SPRING -> {
                world.setGameRule(GameRule.RANDOM_TICK_SPEED, 3);
                world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
            }
            case SUMMER -> {
                world.setGameRule(GameRule.RANDOM_TICK_SPEED, 4);
                world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
            }
            case AUTUMN -> {
                world.setGameRule(GameRule.RANDOM_TICK_SPEED, 3);
                world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
            }
            case WINTER -> {
                world.setGameRule(GameRule.RANDOM_TICK_SPEED, 2);
                world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
            }
        }
    }

    private void applySeasonWeather(World world, Season season) {
        if (world.hasStorm() || world.isThundering()) {
            return;
        }
        Random random = new Random();
        double chance = switch (season) {
            case SPRING -> configManager.getDouble("weather.spring.rain-chance");
            case SUMMER -> configManager.getDouble("weather.summer.clear-chance");
            case AUTUMN -> configManager.getDouble("weather.autumn.rain-chance");
            case WINTER -> configManager.getDouble("weather.winter.snow-chance");
        };
        if (random.nextDouble() < chance) {
            if (season == Season.WINTER) {
                world.setStorm(true);
                world.setWeatherDuration(24000);
            } else if (season == Season.SPRING || season == Season.AUTUMN) {
                world.setStorm(true);
                world.setWeatherDuration(12000);
                world.setThunderDuration(random.nextDouble() < configManager.getDouble("weather.autumn.thunder-chance") ? 200 : 0);
            }
        }
    }

    private void applySeasonBoundEffects(Player player) {
        if (boundManager == null || !boundManager.hasBound(player)) {
            return;
        }
        boundManager.applyActiveEffects(player);
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public SeasonManager getSeasonManager() {
        return seasonManager;
    }

    public BoundManager getBoundManager() {
        return boundManager;
    }

    public SwordManager getSwordManager() {
        return swordManager;
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }

    public UIManager getUIManager() {
        return uiManager;
    }

    public io.github.seasonalsmp.seasonalsmp.effect.SeasonEffectsManager getSeasonEffectsManager() {
        return seasonEffectsManager;
    }
}
