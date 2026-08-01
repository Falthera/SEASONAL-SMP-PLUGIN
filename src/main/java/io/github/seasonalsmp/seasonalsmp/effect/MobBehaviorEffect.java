package io.github.seasonalsmp.seasonalsmp.effect;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import io.github.seasonalsmp.seasonalsmp.effect.particle.ParticleService;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Animals;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

class MobBehaviorEffect implements SeasonEffectsManager.SeasonEffect {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;
    private final Random random;
    private BukkitTask mobTask;

    MobBehaviorEffect(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.random = new Random();
    }

    @Override
    public void apply(Season season) {
        if (mobTask != null && !mobTask.isCancelled()) {
            mobTask.cancel();
        }
        mobTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isEnabled()) {
                    return;
                }
                applyMobBehavior(season);
            }
        }.runTaskTimer(plugin, 0L, 200L);
    }

    private void applyMobBehavior(Season season) {
        for (World world : plugin.getServer().getWorlds()) {
            if (!configManager.getStringList("world.apply-effects-to").contains(world.getName())) {
                continue;
            }
            for (LivingEntity entity : world.getLivingEntities()) {
                if (entity instanceof Player) {
                    continue;
                }
                switch (season) {
                    case SPRING -> applySpringMobEffects(entity);
                    case SUMMER -> applySummerMobEffects(entity);
                    case AUTUMN -> applyAutumnMobEffects(entity);
                    case WINTER -> applyWinterMobEffects(entity);
                }
            }
        }
    }

    private void applySpringMobEffects(LivingEntity entity) {
        if (entity instanceof Animals && random.nextDouble() < 0.05) {
            entity.setHealth(Math.min(entity.getHealth() + 2, entity.getMaxHealth()));
        }
        if (entity.getType() == EntityType.BEE && random.nextDouble() < 0.1) {
            entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation().add(0, 1, 0), 2, 0.3, 0.3, 0.3, 0.05);
        }
    }

    private void applySummerMobEffects(LivingEntity entity) {
        if (entity instanceof Monster) {
            if (random.nextDouble() < 0.2) {
                entity.setHealth(entity.getHealth() * 1.2);
            }
            if (random.nextDouble() < 0.1) {
                entity.getWorld().spawnParticle(Particle.FLAME, entity.getLocation().add(0, 1, 0), 3, 0.4, 0.4, 0.4, 0.05);
            }
        }
    }

    private void applyAutumnMobEffects(LivingEntity entity) {
        if (entity instanceof Monster && random.nextDouble() < 0.15) {
            entity.getWorld().spawnParticle(Particle.CRIT, entity.getLocation().add(0, 1, 0), 3, 0.4, 0.4, 0.4, 0.05);
        }
    }

    private void applyWinterMobEffects(LivingEntity entity) {
        if (entity instanceof Monster && random.nextDouble() < 0.3) {
            entity.getWorld().spawnParticle(Particle.SNOWFLAKE, entity.getLocation().add(0, 1, 0), 2, 0.3, 0.3, 0.3, 0.05);
            if (entity.getLocation().getBlock().getType() == Material.WATER && random.nextDouble() < 0.3) {
                entity.getLocation().getBlock().setType(Material.ICE);
            }
        }
    }

    @Override
    public void remove(Season season) {
        if (mobTask != null && !mobTask.isCancelled()) {
            mobTask.cancel();
            mobTask = null;
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

