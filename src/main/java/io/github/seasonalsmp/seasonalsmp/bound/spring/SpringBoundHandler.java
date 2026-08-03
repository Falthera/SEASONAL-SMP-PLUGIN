package io.github.seasonalsmp.seasonalsmp.bound.spring;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.effect.particle.ParticleService;
import io.github.seasonalsmp.seasonalsmp.effect.sound.SoundService;
import io.github.seasonalsmp.seasonalsmp.trust.TrustManager;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

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
        TrustManager trustManager = plugin.getTrustManager();
        player.setHealth(Math.min(player.getHealth() + healAmount, player.getMaxHealth()));
        player.setFoodLevel(Math.min(player.getFoodLevel() + 6, 20));
        player.setSaturation(Math.min(player.getSaturation() + 6, 20));
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (Set.of(PotionEffectType.POISON, PotionEffectType.WITHER, PotionEffectType.WEAKNESS, PotionEffectType.SLOWNESS, PotionEffectType.BLINDNESS, PotionEffectType.NAUSEA).contains(effect.getType())) {
                player.removePotionEffect(effect.getType());
            }
        }
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player nearby && trustManager.isTrusted(player, nearby)) {
                nearby.setHealth(Math.min(nearby.getHealth() + (healAmount / 2.0), nearby.getMaxHealth()));
                nearby.setFoodLevel(Math.min(nearby.getFoodLevel() + 6, 20));
                nearby.setSaturation(Math.min(nearby.getSaturation() + 6, 20));
                for (PotionEffect effect : nearby.getActivePotionEffects()) {
                    if (Set.of(PotionEffectType.POISON, PotionEffectType.WITHER, PotionEffectType.WEAKNESS, PotionEffectType.SLOWNESS, PotionEffectType.BLINDNESS, PotionEffectType.NAUSEA).contains(effect.getType())) {
                        nearby.removePotionEffect(effect.getType());
                    }
                }
            }
        }
        for (double y = 0; y < 3; y += 0.5) {
            particleService.spawn(center.clone().add(0, y, 0), Particle.HEART, 15, 0.3);
            particleService.spawn(center.clone().add(0, y, 0), Particle.HAPPY_VILLAGER, 10, 0.2);
        }
        particleService.spawnCircle(center, radius, Particle.HEART, 80, 0.8);
        particleService.spawnCircle(center, radius * 0.6, Particle.HAPPY_VILLAGER, 50, 0.6);
        particleService.spawnCircle(center, radius * 1.2, Particle.HEART, 40, 1.2);
        particleService.spawnSphere(center, radius * 0.8, Particle.HEART, 100);
        particleService.spawnSphere(center, radius * 0.4, Particle.HAPPY_VILLAGER, 60);
        center.getWorld().playSound(center, Sound.BLOCK_BEEHIVE_ENTER, 2.0f, 1.5f);
        center.getWorld().playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
    }

    public void activateSwordAbility(Player player) {
        Location center = player.getLocation();
        double radius = configManager.getDouble("swords.spring-sword.ability.radius", 8.0);
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity living) {
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));
                living.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1));
            }
        }
        for (double y = 0; y < 4; y += 0.4) {
            particleService.spawn(center.clone().add(0, y, 0), Particle.HEART, 20, 0.4);
            particleService.spawn(center.clone().add(0, y, 0), Particle.HAPPY_VILLAGER, 15, 0.3);
        }
        particleService.spawnCircle(center, radius, Particle.HEART, 120, 1.0);
        particleService.spawnCircle(center, radius * 0.7, Particle.HAPPY_VILLAGER, 80, 0.8);
        particleService.spawnCircle(center, radius * 1.4, Particle.HEART, 60, 1.4);
        particleService.spawnSphere(center, radius * 0.9, Particle.HEART, 120);
        particleService.spawnSphere(center, radius * 0.5, Particle.HAPPY_VILLAGER, 80);
        center.getWorld().playSound(center, Sound.BLOCK_GRASS_BREAK, 2.0f, 0.8f);
        center.getWorld().playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
    }

    public void applyPassiveEffects(Player player, Season currentSeason) {
        if (player == null || !player.isOnline()) {
            return;
        }
        if (currentSeason == Season.SPRING) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 120, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 144000, 0));
            for (PotionEffect effect : player.getActivePotionEffects()) {
                if (effect.getType() == PotionEffectType.SPEED && effect.getAmplifier() > 0) {
                    player.removePotionEffect(PotionEffectType.SPEED);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 9600, 0));
                }
            }
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 120, 0));
        }
    }
}
