package io.github.seasonalsmp.seasonalsmp.bound.spring;

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

import java.util.Set;

public class SpringBoundHandler {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;
    private final ParticleService particleService;
    private final SoundService soundService;

    public SpringBoundHandler(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.particleService = plugin.getEffectManager().getParticleService();
        this.soundService = plugin.getEffectManager().getSoundService();
    }

    public void activateBoundAbility(Player player) {
        Location center = player.getLocation();
        double radius = configManager.getDouble("bounds.spring-bound.ability-radius", 8.0);
        double healAmount = configManager.getDouble("bounds.spring-bound.heal-amount", 6.0);
        player.setHealth(Math.min(player.getHealth() + healAmount, player.getMaxHealth()));
        player.setFoodLevel(Math.min(player.getFoodLevel() + 6, 20));
        player.setSaturation(Math.min(player.getSaturation() + 6, 20));
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (Set.of(PotionEffectType.POISON, PotionEffectType.WITHER, PotionEffectType.WEAKNESS, PotionEffectType.SLOWNESS, PotionEffectType.BLINDNESS, PotionEffectType.NAUSEA).contains(effect.getType())) {
                player.removePotionEffect(effect.getType());
            }
        }
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity living) {
                living.setHealth(Math.min(living.getHealth() + (healAmount / 2.0), ((LivingEntity) entity).getMaxHealth()));
                for (PotionEffect effect : living.getActivePotionEffects()) {
                    if (Set.of(PotionEffectType.POISON, PotionEffectType.WITHER, PotionEffectType.WEAKNESS, PotionEffectType.SLOWNESS, PotionEffectType.BLINDNESS, PotionEffectType.NAUSEA).contains(effect.getType())) {
                        living.removePotionEffect(effect.getType());
                    }
                }
            }
        }
        particleService.spawnCircle(center, radius, Particle.HEART, 30, 0.5);
        particleService.spawnSphere(center, radius * 0.5, Particle.HAPPY_VILLAGER, 20);
        player.getWorld().playSound(center, Sound.BLOCK_BEEHIVE_ENTER, 2.0f, 1.5f);
    }

    public void activateSwordAbility(Player player) {
        Location center = player.getLocation();
        double radius = configManager.getDouble("swords.spring-sword.ability.radius", 8.0);
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity living && !(entity instanceof Player)) {
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));
                living.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1));
            }
        }
        particleService.spawnCircle(center, radius, Particle.HEART, 40, 0.3);
        particleService.spawnSphere(center, radius * 0.6, Particle.HAPPY_VILLAGER, 30);
        center.getWorld().playSound(center, Sound.BLOCK_GRASS_BREAK, 2.0f, 0.8f);
    }

    public void applyPassiveEffects(Player player, Season currentSeason) {
        if (player == null || !player.isOnline()) {
            return;
        }
        if (currentSeason == Season.SPRING) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 120, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 0));
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 120, 0));
        }
    }
}
