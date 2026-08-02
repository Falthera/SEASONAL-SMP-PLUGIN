package io.github.seasonalsmp.seasonalsmp;

import io.github.seasonalsmp.seasonalsmp.bound.BoundManager;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.command.AbilityCommand;
import io.github.seasonalsmp.seasonalsmp.command.BoundCommand;
import io.github.seasonalsmp.seasonalsmp.command.BoundCommandTabCompleter;
import io.github.seasonalsmp.seasonalsmp.command.ChangeBoundCommand;
import io.github.seasonalsmp.seasonalsmp.command.DebugCommand;
import io.github.seasonalsmp.seasonalsmp.command.EventCommand;
import io.github.seasonalsmp.seasonalsmp.command.GiveSwordCommand;
import io.github.seasonalsmp.seasonalsmp.command.ReloadCommand;
import io.github.seasonalsmp.seasonalsmp.command.SeasonCommand;
import io.github.seasonalsmp.seasonalsmp.command.SeasonCommandTabCompleter;
import io.github.seasonalsmp.seasonalsmp.combat.CombatManager;
import io.github.seasonalsmp.seasonalsmp.combat.CombatListener;
import io.github.seasonalsmp.seasonalsmp.command.SeasonalStartCommand;
import io.github.seasonalsmp.seasonalsmp.command.TrustCommand;
import io.github.seasonalsmp.seasonalsmp.command.DiscordBotDebugCommand;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.core.PluginManager;
import io.github.seasonalsmp.seasonalsmp.effect.EffectManager;
import io.github.seasonalsmp.seasonalsmp.gui.UIManager;
import io.github.seasonalsmp.seasonalsmp.listener.PlayerJoinListener;
import io.github.seasonalsmp.seasonalsmp.listener.SeasonWorldListener;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import io.github.seasonalsmp.seasonalsmp.season.SeasonManager;
import io.github.seasonalsmp.seasonalsmp.seasonalblade.SeasonalBladeListener;
import io.github.seasonalsmp.seasonalsmp.seasonalblade.SeasonalBladeManager;
import io.github.seasonalsmp.seasonalsmp.seasonalblade.LegendaryItemProtectionListener;
import io.github.seasonalsmp.seasonalsmp.seasonalblade.SpearBlockerListener;
import io.github.seasonalsmp.seasonalsmp.sword.SwordCombatListener;
import io.github.seasonalsmp.seasonalsmp.sword.SwordManager;
import io.github.seasonalsmp.seasonalsmp.whitelist.WhitelistAPIServer;
import io.github.seasonalsmp.seasonalsmp.whitelist.WhitelistManager;
import io.github.seasonalsmp.seasonalsmp.trust.TrustManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Random;

public final class SeasonalSMP extends JavaPlugin {

    private static SeasonalSMP instance;
    private PluginManager pluginManager;
    private ConfigManager configManager;
    private SeasonManager seasonManager;
    private BoundManager boundManager;
    private SwordManager swordManager;
    private SeasonalBladeManager seasonalBladeManager;
    private EffectManager effectManager;
    private UIManager uiManager;
    private io.github.seasonalsmp.seasonalsmp.effect.SeasonEffectsManager seasonEffectsManager;
    private io.github.seasonalsmp.seasonalsmp.data.DataStorage dataStorage;
    private CombatManager combatManager;
    private TrustManager trustManager;
    private WhitelistManager whitelistManager;
    private WhitelistAPIServer whitelistAPIServer;
    private BukkitTask seasonCycleTask;
    private BukkitTask ambientEffectTask;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        instance = this;
        long startTime = System.nanoTime();
        try {
            saveDefaultConfig();
            pluginManager = new PluginManager(this);
            configManager = new ConfigManager(this);
            uiManager = new UIManager(this);
            dataStorage = new io.github.seasonalsmp.seasonalsmp.data.DataStorage(this);
            seasonManager = new SeasonManager(this);
            effectManager = new EffectManager(this);
            swordManager = new SwordManager(this);
            seasonalBladeManager = new SeasonalBladeManager(this);
            boundManager = new BoundManager(this, dataStorage);
            configManager.loadAll();
            dataStorage.initialize();
            seasonManager.initialize();
            boundManager.loadAll();
            swordManager.initialize();
            effectManager.initialize();
            uiManager.initialize();
            seasonEffectsManager = new io.github.seasonalsmp.seasonalsmp.effect.SeasonEffectsManager(this);
            seasonEffectsManager.initialize();
            combatManager = new CombatManager(this);
            trustManager = new TrustManager(this);
            whitelistManager = new WhitelistManager(this);
            whitelistAPIServer = new WhitelistAPIServer(this, whitelistManager);
            whitelistAPIServer.start();
            registerListeners();
            registerCommands();
            registerSwordRecipes();
            registerSeasonalBladeRecipe();
            startSeasonCycle();
            startAmbientEffects();
        } catch (Exception e) {
            getLogger().log(java.util.logging.Level.SEVERE, "Failed to enable SeasonalSMP", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;
        getLogger().info("SeasonalSMP v" + getDescription().getVersion() + " enabled in " + elapsedMs + "ms");
        Bukkit.broadcast(Component.text(""));
        Bukkit.broadcast(miniMessage.deserialize("<gold><bold>Seasonal SMP</bold></gold> <yellow>has been enabled!</yellow>"));
        Bukkit.broadcast(miniMessage.deserialize("<gray>Current season: <white>" + seasonManager.getCurrentSeason().getDisplayName() + "</white></gray>"));
        Bukkit.broadcast(miniMessage.deserialize("<gray>Next change in: <white>2 hours</white></gray>"));
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
        if (combatManager != null) {
            combatManager.shutdown();
        }
        if (whitelistAPIServer != null) {
            whitelistAPIServer.stop();
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
        Bukkit.getPluginManager().registerEvents(new CombatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SeasonalBladeListener(this, seasonalBladeManager), this);
        Bukkit.getPluginManager().registerEvents(new LegendaryItemProtectionListener(this, swordManager), this);
        Bukkit.getPluginManager().registerEvents(new SpearBlockerListener(this), this);
        io.github.seasonalsmp.seasonalsmp.event.RelicPurgeListener relicListener = new io.github.seasonalsmp.seasonalsmp.event.RelicPurgeListener(this);
        relicListener.initialize();
    }

    private void registerCommands() {
        org.bukkit.command.PluginCommand season = getCommand("season");
        if (season != null) {
            season.setExecutor(new SeasonCommand(this));
            season.setTabCompleter(new SeasonCommandTabCompleter(this));
        }
        org.bukkit.command.PluginCommand bound = getCommand("bound");
        if (bound != null) {
            bound.setExecutor(new BoundCommand(this));
            bound.setTabCompleter(new BoundCommandTabCompleter(this));
        }
        org.bukkit.command.PluginCommand givesword = getCommand("givesword");
        if (givesword != null) {
            givesword.setExecutor(new GiveSwordCommand(this));
        }
        org.bukkit.command.PluginCommand reload = getCommand("seasonreload");
        if (reload != null) {
            reload.setExecutor(new ReloadCommand(this));
        }
        org.bukkit.command.PluginCommand debug = getCommand("seasondebug");
        if (debug != null) {
            debug.setExecutor(new DebugCommand(this));
        }
        org.bukkit.command.PluginCommand ability = getCommand("ability");
        if (ability != null) {
            ability.setExecutor(new AbilityCommand(this));
        }
        org.bukkit.command.PluginCommand changebound = getCommand("changebound");
        if (changebound != null) {
            changebound.setExecutor(new ChangeBoundCommand(this));
        }
        org.bukkit.command.PluginCommand event = getCommand("event");
        if (event != null) {
            event.setExecutor(new EventCommand(this));
        }
        org.bukkit.command.PluginCommand seasonal = getCommand("seasonal");
        if (seasonal != null) {
            seasonal.setExecutor(new SeasonalStartCommand(this));
        }
        org.bukkit.command.PluginCommand discordBotDebug = getCommand("seasonalsmpdiscordbotdebug");
        if (discordBotDebug != null) {
            discordBotDebug.setExecutor(new DiscordBotDebugCommand(this));
        }
        org.bukkit.command.PluginCommand trust = getCommand("trust");
        if (trust != null) {
            trust.setExecutor(new TrustCommand(this));
        }
    }

    private void registerSwordRecipes() {
        registerSwordRecipe(BoundType.SPRING, "spring_sword", new String[]{" R ", "S H", " N "},
                Map.of('S', Material.DIAMOND_SWORD, 'R', Material.RED_TULIP, 'H', Material.HONEY_BOTTLE, 'N', Material.OAK_SAPLING));
        registerSwordRecipe(BoundType.SUMMER, "summer_sword", new String[]{" B ", "S M", " F "},
                Map.of('S', Material.DIAMOND_SWORD, 'B', Material.BLAZE_ROD, 'M', Material.MAGMA_BLOCK, 'F', Material.FIRE_CHARGE));
        registerSwordRecipe(BoundType.AUTUMN, "autumn_sword", new String[]{" G ", "S P", " H "},
                Map.of('S', Material.DIAMOND_SWORD, 'G', Material.GOLDEN_CARROT, 'P', Material.PUMPKIN, 'H', Material.HAY_BLOCK));
        registerSwordRecipe(BoundType.WINTER, "winter_sword", new String[]{" I ", "BSN", "   "},
                Map.of('S', Material.DIAMOND_SWORD, 'I', Material.ICE, 'B', Material.BLUE_ICE, 'N', Material.SNOW_BLOCK));
    }

    private void registerSwordRecipe(BoundType bound, String key, String[] shape, Map<Character, Material> ingredients) {
        ItemStack result = getSwordManager().buildSword(bound);
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this, key), result);
        recipe.shape(shape);
        for (Map.Entry<Character, Material> entry : ingredients.entrySet()) {
            recipe.setIngredient(entry.getKey(), entry.getValue());
        }
        Bukkit.addRecipe(recipe);
    }

    private void registerSeasonalBladeRecipe() {
        ItemStack result = getSeasonalBladeManager().buildSeasonalBlade();
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this, "seasonal_blade"), result);
        recipe.shape(" B ", "HMH", " F ");
        recipe.setIngredient('H', Material.DIAMOND_SWORD);
        recipe.setIngredient('M', Material.MACE);
        recipe.setIngredient('B', Material.BLAZE_ROD);
        recipe.setIngredient('F', Material.FIRE_CHARGE);
        Bukkit.addRecipe(recipe);
    }

    private void startSeasonCycle() {
        long durationSeconds = configManager.getLong("season.duration-seconds", 7200);
        long cycleTicks = durationSeconds * 20L;
        long warningSeconds = configManager.getLong("season.transition-announce-seconds", 30);
        long warningTicks = warningSeconds * 20L;
        seasonCycleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!seasonManager.advanceSeason()) {
                    return;
                }
                applySeasonChange();
            }
        }.runTaskTimer(this, cycleTicks, cycleTicks);
        if (warningTicks > 0 && warningTicks < cycleTicks) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isEnabled() || seasonManager == null) {
                        return;
                    }
                    Season next = seasonManager.getNextSeason();
                    Bukkit.broadcastMessage("§6§lSeason changing to " + next.getDisplayName() + " in " + warningSeconds + " seconds!");
                }
            }.runTaskTimer(this, cycleTicks - warningTicks, cycleTicks);
        }
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
        if (season == Season.WINTER && !canWorldSnow(world)) {
            return;
        }
        if (random.nextDouble() < chance) {
            int duration;
            switch (season) {
                case WINTER -> duration = random.nextInt(20000) + 24000;
                case SPRING, AUTUMN -> duration = random.nextInt(16000) + 12000;
                case SUMMER -> duration = random.nextInt(8000) + 4000;
                default -> duration = 12000;
            }
            if (season == Season.SPRING || season == Season.AUTUMN) {
                world.setStorm(true);
                world.setWeatherDuration(duration);
                double thunderChance = season == Season.AUTUMN
                        ? configManager.getDouble("weather.autumn.thunder-chance")
                        : configManager.getDouble("weather.spring.thunder-chance");
                int thunderDuration = random.nextDouble() < thunderChance ? random.nextInt(400) + 200 : 0;
                world.setThunderDuration(thunderDuration);
            } else if (season == Season.SUMMER) {
                if (random.nextDouble() < 0.5) {
                    world.setStorm(true);
                    world.setWeatherDuration(duration);
                    world.setThunderDuration(random.nextInt(600) + 300);
                }
            }
        }
    }

    private boolean canWorldSnow(World world) {
        org.bukkit.block.Block spawnBlock = world.getSpawnLocation().getBlock();
        double temperature = spawnBlock.getTemperature();
        if (temperature < 0.15) {
            return true;
        }
        for (Player player : world.getPlayers()) {
            if (player.getLocation().getBlock().getTemperature() < 0.15) {
                return true;
            }
        }
        return false;
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

    public SeasonalBladeManager getSeasonalBladeManager() {
        return seasonalBladeManager;
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

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public WhitelistManager getWhitelistManager() {
        return whitelistManager;
    }

    public TrustManager getTrustManager() {
        return trustManager;
    }
}
