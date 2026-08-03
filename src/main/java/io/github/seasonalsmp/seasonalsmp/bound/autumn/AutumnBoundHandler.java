package io.github.seasonalsmp.seasonalsmp.bound.autumn;

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

public class AutumnBoundHandler {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;
    private final ParticleService particleService;
    private final SoundService soundService;

    public AutumnBoundHandler(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.particleService = plugin.getEffectManager().getParticleService();
        this.soundService = plugin.getEffectManager().getSoundService();
    }

    public void activateBoundAbility(Player player) {
        int cropRadius = configManager.getInt("bounds.autumn-bound.crop-radius", 6);
        Location center = player.getLocation();
        for (int x = -cropRadius; x <= cropRadius; x++) {
            for (int y = -cropRadius; y <= cropRadius; y++) {
                for (int z = -cropRadius; z <= cropRadius; z++) {
                    if (Math.sqrt(x*x + y*y + z*z) > cropRadius) continue;
                    org.bukkit.block.Block block = center.clone().add(x, y, z).getBlock();
                    if (block.getBlockData() instanceof org.bukkit.block.data.Ageable ageable && ageable.getAge() == ageable.getMaximumAge()) {
                        block.breakNaturally(true, true);
                    }
                }
            }
        }
        for (double y = 0; y < 3; y += 0.5) {
            particleService.spawn(center.clone().add(0, y, 0), Particle.CRIT, 25, 0.6);
            particleService.spawn(center.clone().add(0, y, 0), Particle.SOUL, 15, 0.4);
            particleService.spawn(center.clone().add(0, y, 0), Particle.TOTEM_OF_UNDYING, 10, 0.3);
        }
        particleService.spawnCircle(center, cropRadius, Particle.CRIT, 100, 1.0);
        particleService.spawnCircle(center, cropRadius * 0.7, Particle.SOUL, 70, 0.8);
        particleService.spawnCircle(center, cropRadius * 1.3, Particle.TOTEM_OF_UNDYING, 50, 1.2);
        particleService.spawnSphere(center, cropRadius * 0.8, Particle.CRIT, 120);
        particleService.spawnSphere(center, cropRadius * 0.5, Particle.SOUL, 80);
        center.getWorld().playSound(center, Sound.BLOCK_CROP_BREAK, 2.0f, 0.8f);
        center.getWorld().playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
    }

    public void activateSwordAbility(Player player) {
        Location center = player.getLocation();
        double radius = configManager.getDouble("swords.autumn-sword.ability.radius", 10.0);
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity living) {
                if (living.getHealth() < living.getMaxHealth() * 0.3) {
                    living.setHealth(0);
                    particleService.spawn(living.getLocation(), Particle.TOTEM_OF_UNDYING, 40, 1.0);
                    particleService.spawn(living.getLocation(), Particle.SOUL, 30, 0.8);
                }
            }
        }
        for (double y = 0; y < 4; y += 0.5) {
            particleService.spawn(center.clone().add(0, y, 0), Particle.CRIT, 20, 0.5);
            particleService.spawn(center.clone().add(0, y, 0), Particle.SOUL, 15, 0.4);
            particleService.spawn(center.clone().add(0, y, 0), Particle.TOTEM_OF_UNDYING, 10, 0.3);
        }
        particleService.spawnCircle(center, radius, Particle.SOUL, 120, 1.0);
        particleService.spawnCircle(center, radius * 0.7, Particle.CRIT, 90, 0.8);
        particleService.spawnCircle(center, radius * 1.2, Particle.TOTEM_OF_UNDYING, 70, 1.2);
        particleService.spawnSphere(center, radius * 0.8, Particle.SOUL, 120);
        particleService.spawnSphere(center, radius * 0.4, Particle.TOTEM_OF_UNDYING, 80);
        center.getWorld().playSound(center, Sound.ENTITY_WITHER_DEATH, 2.0f, 1.0f);
        center.getWorld().playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
    }

    public void applyPassiveEffects(Player player, Season currentSeason) {
        if (player == null || !player.isOnline()) {
            return;
        }
        if (currentSeason == Season.AUTUMN) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 120, 0));
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 0));
        }
    }
}
