package io.github.seasonalsmp.seasonalsmp.effect.particle;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Random;

public class ParticleService {

    private final SeasonalSMP plugin;
    private final boolean enabled;
    private final int maxParticlesPerSecond;
    private final Random random;

    public ParticleService(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfigManager().getBoolean("effects.ambient-vfx-enabled");
        this.maxParticlesPerSecond = plugin.getConfigManager().getInt("effects.particles-per-second-max", 500);
        this.random = new Random();
    }

    public void spawn(Location location, Particle particle, int count, double speed) {
        spawn(location, particle, count, speed, null);
    }

    public void spawn(Location location, Particle particle, int count, double speed, org.bukkit.Color color) {
        if (!enabled || location == null || location.getWorld() == null) {
            return;
        }
        if (count <= 0) {
            return;
        }
        if (count > maxParticlesPerSecond) {
            count = maxParticlesPerSecond;
        }
        try {
            if (color != null && particle.getDataType() == Particle.DustOptions.class) {
                location.getWorld().spawnParticle(particle, location, count, 0.5, 0.5, 0.5, speed,
                    new Particle.DustOptions(color, 1.5f));
            } else if (particle == Particle.DUST_COLOR_TRANSITION) {
                location.getWorld().spawnParticle(particle, location, count, 0.5, 0.5, 0.5, speed, new Particle.DustTransition(color, color, 1.0f));
            } else {
                location.getWorld().spawnParticle(particle, location, count, 0.5, 0.5, 0.5, speed);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn particle " + particle.name() + ": " + e.getMessage());
        }
    }

    public void spawnCircle(Location center, double radius, Particle particle, int count, double speed) {
        spawnCircle(center, radius, particle, count, speed, null);
    }

    public void spawnCircle(Location center, double radius, Particle particle, int count, double speed, org.bukkit.Color color) {
        if (!enabled || center == null || center.getWorld() == null) {
            return;
        }
        int points = Math.min(count, 64);
        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i) / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location loc = center.clone().add(x, 0, z);
            try {
                if (color != null && particle.getDataType() == Particle.DustOptions.class) {
                    loc.getWorld().spawnParticle(particle, loc, 1, 0.0, 0.0, 0.0, speed,
                        new Particle.DustOptions(color, 1.0f));
                } else {
                    loc.getWorld().spawnParticle(particle, loc, 1, 0.0, 0.0, 0.0, speed);
                }
            } catch (Exception e) {
                // Ignore particle errors
            }
        }
    }

    public void spawnHelix(Location base, double height, double radius, Particle particle, int count, double speed) {
        spawnHelix(base, height, radius, particle, count, speed, null);
    }

    public void spawnHelix(Location base, double height, double radius, Particle particle, int count, double speed, org.bukkit.Color color) {
        if (!enabled || base == null || base.getWorld() == null) {
            return;
        }
        int points = Math.min(count, 64);
        for (int i = 0; i < points; i++) {
            double t = (double) i / (double) points;
            double angle = t * Math.PI * 4;
            double y = base.getY() + t * height;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location loc = base.clone().add(x, y - base.getY(), z);
            try {
                if (color != null && particle.getDataType() == Particle.DustOptions.class) {
                    loc.getWorld().spawnParticle(particle, loc, 1, 0.0, 0.0, 0.0, speed,
                        new Particle.DustOptions(color, 1.5f));
                } else {
                    loc.getWorld().spawnParticle(particle, loc, 1, 0.0, 0.0, 0.0, speed);
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    public void spawnSphere(Location center, double radius, Particle particle, int count) {
        spawnSphere(center, radius, particle, count, null);
    }

    public void spawnSphere(Location center, double radius, Particle particle, int count, org.bukkit.Color color) {
        if (!enabled || center == null || center.getWorld() == null) {
            return;
        }
        int points = Math.min(count, 128);
        for (int i = 0; i < points; i++) {
            double phi = Math.acos(2 * random.nextDouble() - 1);
            double theta = 2 * Math.PI * random.nextDouble();
            double x = radius * Math.sin(phi) * Math.cos(theta);
            double y = radius * Math.sin(phi) * Math.sin(theta);
            double z = radius * Math.cos(phi);
            Location loc = center.clone().add(x, y, z);
            try {
                if (color != null && particle.getDataType() == Particle.DustOptions.class) {
                    loc.getWorld().spawnParticle(particle, loc, 1, 0.0, 0.0, 0.0, 0.0,
                        new Particle.DustOptions(color, 2.0f));
                } else {
                    loc.getWorld().spawnParticle(particle, loc, 1, 0.0, 0.0, 0.0, 0.0);
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    public void broadcastParticle(Player center, Particle particle, int count, double speed, double range) {
        if (!enabled || center == null || !center.isOnline()) {
            return;
        }
        for (Player player : center.getWorld().getPlayers()) {
            if (player.getLocation().distance(center.getLocation()) <= range) {
                player.spawnParticle(particle, center.getLocation(), count, 0.5, 0.5, 0.5, speed);
            }
        }
    }
}
