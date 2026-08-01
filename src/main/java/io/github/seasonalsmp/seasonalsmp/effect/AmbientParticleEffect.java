package io.github.seasonalsmp.seasonalsmp.effect;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import io.github.seasonalsmp.seasonalsmp.effect.particle.ParticleService;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

class AmbientParticleEffect implements SeasonEffectsManager.SeasonEffect {

    private final SeasonalSMP plugin;
    private final ParticleService particleService;
    private final Random random;
    private final ConcurrentHashMap<org.bukkit.entity.Player, BukkitTask> playerTasks;
    private final ConfigManager cfg;

    AmbientParticleEffect(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.particleService = plugin.getEffectManager().getParticleService();
        this.random = new Random();
        this.playerTasks = new ConcurrentHashMap<>();
        this.cfg = plugin.getConfigManager();
    }

    @Override
    public void apply(Season season) {
        clearAllTasks();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            startPlayerAmbience(player, season);
        }
    }

    private void clearAllTasks() {
        for (BukkitTask task : playerTasks.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        playerTasks.clear();
    }

    private void startPlayerAmbience(Player player, Season season) {
        BukkitTask existing = playerTasks.remove(player.getUniqueId());
        if (existing != null && !existing.isCancelled()) {
            existing.cancel();
        }
        BukkitTask task = new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                spawnAmbientParticles(player, season);
            }
        }.runTaskTimer(plugin, 0L, 4L);
        playerTasks.put(player, task);
    }

    private void spawnAmbientParticles(Player player, Season season) {
        org.bukkit.Location loc = player.getLocation();
        switch (season) {
            case SPRING -> spawnSpringParticles(loc);
            case SUMMER -> spawnSummerParticles(loc);
            case AUTUMN -> spawnAutumnParticles(loc);
            case WINTER -> spawnWinterParticles(loc);
        }
    }

    private void spawnSpringParticles(org.bukkit.Location loc) {
        int count = cfg.getInt("world-transformation.spring-flower-particle-count", 30);
        for (int i = 0; i < count; i++) {
            double x = (random.nextDouble() - 0.5) * 14;
            double z = (random.nextDouble() - 0.5) * 14;
            double y = random.nextDouble() * 4;
            org.bukkit.Location p = loc.clone().add(x, y, z);
            particleService.spawn(p, Particle.HEART, 1, 0.0);
            if (random.nextDouble() < 0.4) {
                particleService.spawn(p, Particle.FALLING_NECTAR, 1, 0.0);
            }
        }
    }

    private void spawnSummerParticles(org.bukkit.Location loc) {
        int count = cfg.getInt("world-transformation.summer-ember-particle-count", 50);
        for (int i = 0; i < count; i++) {
            double x = (random.nextDouble() - 0.5) * 16;
            double z = (random.nextDouble() - 0.5) * 16;
            double y = random.nextDouble() * 3 + 1;
            org.bukkit.Location p = loc.clone().add(x, y, z);
            particleService.spawn(p, Particle.FLAME, 1, 0.0);
            if (random.nextDouble() < 0.3) {
                particleService.spawn(p, Particle.LAVA, 1, 0.0);
            }
        }
    }

    private void spawnAutumnParticles(org.bukkit.Location loc) {
        int count = cfg.getInt("world-transformation.autumn-leaf-particle-count", 40);
        for (int i = 0; i < count; i++) {
            double x = (random.nextDouble() - 0.5) * 16;
            double z = (random.nextDouble() - 0.5) * 16;
            double y = random.nextDouble() * 5 + 2;
            org.bukkit.Location p = loc.clone().add(x, y, z);
            particleService.spawn(p, Particle.CRIT, 1, 0.0);
            if (random.nextDouble() < 0.3) {
                particleService.spawn(p, Particle.CLOUD, 1, 0.0);
            }
        }
    }

    private void spawnWinterParticles(org.bukkit.Location loc) {
        int count = cfg.getInt("world-transformation.winter-snow-particle-count", 60);
        for (int i = 0; i < count; i++) {
            double x = (random.nextDouble() - 0.5) * 18;
            double z = (random.nextDouble() - 0.5) * 18;
            double y = 6;
            org.bukkit.Location p = loc.clone().add(x, y, z);
            particleService.spawn(p, Particle.SNOWFLAKE, 1, 0.0);
            if (random.nextDouble() < 0.2) {
                particleService.spawn(p, Particle.CLOUD, 1, 0.0);
            }
        }
    }

    @Override
    public void remove(Season season) {
        clearAllTasks();
    }

    @Override
    public boolean isEnabled() {
        return cfg.getBoolean("effects.ambient-vfx-enabled");
    }
}
