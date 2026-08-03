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
        particleService.spawn(center, Particle.CRIT, 40, 0.8);
        particleService.spawnCircle(center, cropRadius, Particle.CRIT, 30, 0.5);
        center.getWorld().playSound(center, Sound.BLOCK_CROP_BREAK, 2.0f, 0.8f);
    }

    public void activateSwordAbility(Player player) {
        Location center = player.getLocation();
        double radius = configManager.getDouble("swords.autumn-sword.ability.radius", 10.0);
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity living) {
                if (living.getHealth() < living.getMaxHealth() * 0.3) {
                    living.setHealth(0);
                    particleService.spawn(living.getLocation(), Particle.CRIT, 20, 0.5);
                }
            }
        }
        particleService.spawnCircle(center, radius, Particle.SOUL, 50, 0.4);
        particleService.spawnSphere(center, radius * 0.5, Particle.TOTEM_OF_UNDYING, 25);
        center.getWorld().playSound(center, Sound.ENTITY_WITHER_DEATH, 2.0f, 1.0f);
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
