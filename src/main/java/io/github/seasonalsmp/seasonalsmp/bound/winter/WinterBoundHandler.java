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
        particleService.spawnCircle(center, 3.0, Particle.SNOWFLAKE, 60, 0.4);
        particleService.spawnSphere(center, 2.5, Particle.SNOWFLAKE, 40);
        center.getWorld().playSound(center, Sound.BLOCK_GLASS_BREAK, 2.0f, 0.6f);
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
        particleService.spawnCircle(center, radius, Particle.SNOWFLAKE, 80, 0.3);
        particleService.spawnSphere(center, radius * 0.7, Particle.CLOUD, 50);
        center.getWorld().playSound(center, Sound.BLOCK_GLASS_BREAK, 2.0f, 0.5f);
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
