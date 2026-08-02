package io.github.seasonalsmp.seasonalsmp.bound.summer;

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

public class SummerBoundHandler {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;
    private final ParticleService particleService;
    private final SoundService soundService;

    public SummerBoundHandler(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.particleService = plugin.getEffectManager().getParticleService();
        this.soundService = plugin.getEffectManager().getSoundService();
    }

    public void activateBoundAbility(Player player) {
        Location center = player.getLocation();
        double radius = configManager.getDouble("bounds.summer-bound.ability-radius", 7.0);
        double damage = configManager.getDouble("bounds.summer-bound.damage", 6.0);
        double igniteSeconds = configManager.getDouble("bounds.summer-bound.ignite-seconds", 4.0);
        double knockback = configManager.getDouble("bounds.summer-bound.knockback-strength", 1.5);
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity living) {
                living.damage(damage, player);
                living.setFireTicks((int) (igniteSeconds * 20));
                Vector direction = entity.getLocation().toVector().subtract(center.toVector()).normalize();
                if (direction.length() == 0) {
                    direction = new Vector(0, 0, 1);
                }
                entity.setVelocity(direction.multiply(knockback));
            }
        }
        particleService.spawn(player.getLocation(), Particle.FLAME, 50, 1.0);
        particleService.spawnCircle(center, radius, Particle.FLAME, 20, 0.8);
        particleService.spawn(player.getLocation(), Particle.LAVA, 10, 0.3);
        center.getWorld().playSound(center, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 1.2f);
    }

    public void activateSwordAbility(Player player) {
        Location center = player.getLocation();
        double radius = configManager.getDouble("swords.summer-sword.ability.radius", 9.0);
        double damage = configManager.getDouble("swords.summer-sword.ability.damage", 12.0);
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity living) {
                living.damage(damage, player);
                living.setFireTicks(60);
            }
        }
        for (double y = center.getY() + 1; y < center.getY() + 12; y += 0.5) {
            particleService.spawn(center.clone().add(0, y - center.getY(), 0), Particle.END_ROD, 2, 0.2);
        }
        particleService.spawnCircle(center, radius, Particle.SOUL_FIRE_FLAME, 30, 0.6);
        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.8f);
    }

    public void applyPassiveEffects(Player player, Season currentSeason) {
        if (player == null || !player.isOnline()) {
            return;
        }
        if (currentSeason == Season.SUMMER) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 120, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 120, 0));
            meltNearbyIce(player);
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 0));
        }
    }

    private void meltNearbyIce(Player player) {
        Location center = player.getLocation();
        int radius = 4;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    org.bukkit.block.Block block = center.clone().add(x, y, z).getBlock();
                    org.bukkit.Material type = block.getType();
                    if (type == org.bukkit.Material.ICE || type == org.bukkit.Material.FROSTED_ICE || type == org.bukkit.Material.BLUE_ICE) {
                        block.setType(org.bukkit.Material.WATER);
                    } else if (type == org.bukkit.Material.SNOW_BLOCK || type == org.bukkit.Material.SNOW) {
                        block.setType(org.bukkit.Material.AIR);
                    } else if (type == org.bukkit.Material.POWDER_SNOW) {
                        block.setType(org.bukkit.Material.AIR);
                    }
                }
            }
        }
    }
}
