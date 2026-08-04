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
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AutumnBoundHandler {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;
    private final ParticleService particleService;
    private final SoundService soundService;
    private final Map<UUID, Integer> leafStacks;

    public AutumnBoundHandler(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.particleService = plugin.getEffectManager().getParticleService();
        this.soundService = plugin.getEffectManager().getSoundService();
        this.leafStacks = new ConcurrentHashMap<>();
    }

    public void onEntityHit(Entity entity) {
        if (entity == null || entity.isDead() || !(entity instanceof LivingEntity)) {
            return;
        }
        UUID entityId = entity.getUniqueId();
        int current = leafStacks.getOrDefault(entityId, 0);
        if (current < 5) {
            leafStacks.put(entityId, current + 1);
        }
        Location loc = entity.getLocation().add(0, 1, 0);
        particleService.spawn(loc, Particle.CRIT, 10, 0.3);
        particleService.spawn(loc, Particle.SOUL, 5, 0.2);
    }

    public int getStacks(Entity entity) {
        if (entity == null) {
            return 0;
        }
        return leafStacks.getOrDefault(entity.getUniqueId(), 0);
    }

    public void consumeStacks(Entity entity) {
        if (entity == null) {
            return;
        }
        leafStacks.remove(entity.getUniqueId());
    }

    public void activateBoundAbility(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        Location center = player.getLocation();
        double radius = configManager.getDouble("bounds.autumn-bound.ability-radius", 8.0);
        List<Entity> marked = new ArrayList<>();
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            int stacks = getStacks(entity);
            if (stacks > 0 && entity instanceof LivingEntity living) {
                marked.add(entity);
                switch (stacks) {
                    case 1 -> living.damage(1.0, player);
                    case 2 -> living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 0));
                    case 3 -> living.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0));
                    case 4 -> living.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                    case 5 -> {
                        living.damage(8.0, player);
                        Vector knockback = living.getLocation().toVector().subtract(center.toVector()).normalize().multiply(1.5);
                        living.setVelocity(knockback);
                        particleService.spawn(living.getLocation(), Particle.EXPLOSION_EMITTER, 1, 0);
                        particleService.spawn(living.getLocation(), Particle.CRIT, 40, 1.0);
                        particleService.spawn(living.getLocation(), Particle.SOUL, 30, 0.8);
                    }
                }
                particleService.spawn(living.getLocation().add(0, 1, 0), Particle.CRIT, 15, 0.4);
                particleService.spawn(living.getLocation().add(0, 1, 0), Particle.SOUL, 10, 0.3);
                consumeStacks(entity);
            }
        }
        if (!marked.isEmpty()) {
            particleService.spawnCircle(center, radius, Particle.CRIT, 80, 1.0);
            particleService.spawnCircle(center, radius * 0.7, Particle.SOUL, 70, 0.8);
            particleService.spawnSphere(center, radius * 0.8, Particle.CRIT, 60);
            center.getWorld().playSound(center, Sound.ENTITY_WITHER_DEATH, 2.0f, 0.8f);
            center.getWorld().playSound(center, Sound.BLOCK_GRASS_BREAK, 2.0f, 1.2f);
        }
        player.sendMessage("§6§lAutumn's End §7consumed §e" + marked.size() + " §7marked targets!");
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
