package io.github.seasonalsmp.seasonalsmp.effect;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

class BlockStateEffect implements SeasonEffectsManager.SeasonEffect {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;
    private final Random random;
    private BukkitTask freezeTask;
    private final java.util.concurrent.ConcurrentHashMap<org.bukkit.Location, Material> freezeHistory;

    BlockStateEffect(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.random = new Random();
        this.freezeHistory = new java.util.concurrent.ConcurrentHashMap<>();
    }

    @Override
    public void apply(Season season) {
        if (freezeTask != null && !freezeTask.isCancelled()) {
            freezeTask.cancel();
        }
        if (season == Season.WINTER) {
            freezeTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!plugin.isEnabled()) {
                        return;
                    }
                    freezeSurfaceWater();
                }
            }.runTaskTimer(plugin, 0L, configManager.getLong("world-transformation.freeze-task-interval-ticks", 200));
        } else if (season == Season.SPRING) {
            meltSeasonalIce();
        }
    }

    private void freezeSurfaceWater() {
        int radius = configManager.getInt("world-transformation.freeze-check-radius", 48);
        int checksPerPlayer = 25;
        for (World world : plugin.getServer().getWorlds()) {
            if (!configManager.getStringList("world.apply-effects-to").contains(world.getName())) {
                continue;
            }
            for (Player player : world.getPlayers()) {
                org.bukkit.Location loc = player.getLocation();
                int px = loc.getBlockX();
                int py = loc.getBlockY();
                int pz = loc.getBlockZ();
                int checked = 0;
                for (int x = px - radius; x <= px + radius && checked < checksPerPlayer; x += 3) {
                    for (int z = pz - radius; z <= pz + radius && checked < checksPerPlayer; z += 3) {
                        if (random.nextInt(8) != 0) {
                            continue;
                        }
                        for (int y = py + 3; y >= py - 6 && checked < checksPerPlayer; y--) {
                            Block block = world.getBlockAt(x, y, z);
                            if (block.getType() == Material.WATER && block.getBlockData() instanceof org.bukkit.block.data.Levelled levelled && levelled.getLevel() == 0) {
                                if (isExposedToAir(block)) {
                                    block.setType(Material.ICE);
                                    freezeHistory.put(block.getLocation(), Material.WATER);
                                    checked++;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isExposedToAir(Block block) {
        return block.getRelative(0, 1, 0).getType().isAir();
    }

    private void meltSeasonalIce() {
        for (java.util.Map.Entry<org.bukkit.Location, Material> entry : freezeHistory.entrySet()) {
            org.bukkit.Location loc = entry.getKey();
            if (!loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                continue;
            }
            Block block = loc.getBlock();
            if (block.getType() == Material.ICE) {
                block.setType(Material.WATER);
            }
        }
        freezeHistory.clear();
    }

    @Override
    public void remove(Season season) {
        if (freezeTask != null && !freezeTask.isCancelled()) {
            freezeTask.cancel();
            freezeTask = null;
        }
        if (season == Season.WINTER) {
            meltSeasonalIce();
        }
    }

    @Override
    public boolean isEnabled() {
        return configManager.getBoolean("world.apply-visual-effects");
    }
}
