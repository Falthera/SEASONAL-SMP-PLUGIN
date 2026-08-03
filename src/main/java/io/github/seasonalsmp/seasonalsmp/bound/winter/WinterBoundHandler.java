package io.github.seasonalsmp.seasonalsmp.bound.winter;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.effect.particle.ParticleService;
import io.github.seasonalsmp.seasonalsmp.effect.sound.SoundService;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WinterBoundHandler {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;
    private final ParticleService particleService;
    private final SoundService soundService;

    public WinterBoundHandler(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.particleService = plugin.getEffectManager().getParticleService();
        this.soundService = plugin.getEffectManager().getSoundService();
    }

    public void activateBoundAbility(Player player) {
        int durationSeconds = configManager.getInt("bounds.winter-bound.duration-seconds", 8);
        int resistanceLevel = configManager.getInt("bounds.winter-bound.resistance-level", 3);
        int slowSeconds = configManager.getInt("bounds.winter-bound.slow-duration-seconds", 5);
        int slowAmplifier = configManager.getInt("bounds.winter-bound.slow-amplifier", 2);
        double slowRadius = configManager.getDouble("bounds.winter-bound.slow-radius", 6.0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, durationSeconds * 20, resistanceLevel - 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, durationSeconds * 20, resistanceLevel));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, durationSeconds * 20, 1));
        Location center = player.getLocation();
        for (Entity entity : player.getNearbyEntities(slowRadius, slowRadius, slowRadius)) {
            if (entity instanceof LivingEntity living) {
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slowSeconds * 20, slowAmplifier));
            }
        }
        for (double y = 0; y < 4; y += 0.5) {
            particleService.spawn(center.clone().add(0, y, 0), Particle.SNOWFLAKE, 30, 0.6);
            particleService.spawn(center.clone().add(0, y, 0), Particle.CLOUD, 20, 0.4);
            particleService.spawn(center.clone().add(0, y, 0), Particle.END_ROD, 10, 0.2);
        }
        particleService.spawnCircle(center, 3.0, Particle.SNOWFLAKE, 120, 1.0);
        particleService.spawnCircle(center, 2.0, Particle.CLOUD, 80, 0.8);
        particleService.spawnCircle(center, 4.0, Particle.END_ROD, 60, 1.2);
        particleService.spawnSphere(center, 2.5, Particle.SNOWFLAKE, 100);
        particleService.spawnSphere(center, 1.5, Particle.CLOUD, 70);
        center.getWorld().playSound(center, Sound.BLOCK_GLASS_BREAK, 2.0f, 0.6f);
        center.getWorld().playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
    }

    public void activateSwordAbility(Player player) {
        Location center = player.getLocation();
        double radius = configManager.getDouble("swords.winter-sword.ability.radius", 7.0);
        int freezeDuration = configManager.getInt("swords.winter-sword.ability.duration-seconds", 6);
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity living) {
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, freezeDuration * 20, 4));
                living.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, freezeDuration * 20, 3));
                living.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, freezeDuration * 20, 1));
            }
        }
        for (double y = 0; y < 5; y += 0.5) {
            particleService.spawn(center.clone().add(0, y, 0), Particle.SNOWFLAKE, 25, 0.5);
            particleService.spawn(center.clone().add(0, y, 0), Particle.CLOUD, 20, 0.4);
            particleService.spawn(center.clone().add(0, y, 0), Particle.END_ROD, 10, 0.2);
        }
        particleService.spawnCircle(center, radius, Particle.SNOWFLAKE, 140, 1.2);
        particleService.spawnCircle(center, radius * 0.7, Particle.CLOUD, 100, 1.0);
        particleService.spawnCircle(center, radius * 1.3, Particle.END_ROD, 70, 1.4);
        particleService.spawnSphere(center, radius * 0.8, Particle.SNOWFLAKE, 120);
        particleService.spawnSphere(center, radius * 0.5, Particle.CLOUD, 90);
        center.getWorld().playSound(center, Sound.BLOCK_GLASS_BREAK, 2.0f, 0.5f);
        center.getWorld().playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
    }

    public void applyPassiveEffects(Player player, Season currentSeason) {
        if (player == null || !player.isOnline()) {
            return;
        }
        if (currentSeason == Season.WINTER) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 120, 0));
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 120, 0));
        }
    }
}
