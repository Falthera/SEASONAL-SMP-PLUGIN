package io.github.seasonalsmp.seasonalsmp.effect;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
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
                    accumulateSnow();
                }
            }.runTaskTimer(plugin, 0L, configManager.getLong("world-transformation.freeze-task-interval-ticks", 200));
        } else if (season == Season.SPRING) {
            meltSeasonalIce();
            spawnFlowers();
        } else if (season == Season.AUTUMN) {
            autumnLeafDecay();
        } else if (season == Season.SUMMER) {
            placeEmberBlocks();
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

    private void accumulateSnow() {
        for (World world : plugin.getServer().getWorlds()) {
            if (!configManager.getStringList("world.apply-effects-to").contains(world.getName())) {
                continue;
            }
            for (Player player : world.getPlayers()) {
                org.bukkit.Location loc = player.getLocation();
                int px = loc.getBlockX();
                int py = loc.getBlockY();
                int pz = loc.getBlockZ();
                int radius = 12;
                int checked = 0;
                for (int x = px - radius; x <= px + radius && checked < 20; x += 4) {
                    for (int z = pz - radius; z <= pz + radius && checked < 20; z += 4) {
                        for (int y = py + 3; y >= py - 2 && checked < 20; y--) {
                            Block block = world.getBlockAt(x, y, z);
                            if (block.getType() == Material.AIR && isExposedToAir(block)) {
                                if (random.nextInt(40) == 0) {
                                    block.setType(Material.SNOW);
                                }
                                checked++;
                                break;
                            }
                        }
                    }
                }
            }
        }
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

    private void spawnFlowers() {
        for (World world : plugin.getServer().getWorlds()) {
            if (!configManager.getStringList("world.apply-effects-to").contains(world.getName())) {
                continue;
            }
            for (Player player : world.getPlayers()) {
                org.bukkit.Location loc = player.getLocation();
                int px = loc.getBlockX();
                int py = loc.getBlockY();
                int pz = loc.getBlockZ();
                int radius = 16;
                int checked = 0;
                for (int x = px - radius; x <= px + radius && checked < 20; x += 5) {
                    for (int z = pz - radius; z <= pz + radius && checked < 20; z += 5) {
                        for (int y = py; y >= py - 3 && checked < 20; y--) {
                            Block block = world.getBlockAt(x, y, z);
                            if (block.getType() == Material.GRASS_BLOCK && isExposedToAir(block)) {
                                if (random.nextInt(12) == 0) {
                                    Block above = block.getRelative(0, 1, 0);
                                    Material flower = switch (random.nextInt(3)) {
                                        case 0 -> Material.DANDELION;
                                        case 1 -> Material.POPPY;
                                        default -> Material.OXEYE_DAISY;
                                    };
                                    above.setType(flower);
                                }
                                checked++;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void autumnLeafDecay() {
        for (World world : plugin.getServer().getWorlds()) {
            if (!configManager.getStringList("world.apply-effects-to").contains(world.getName())) {
                continue;
            }
            for (Player player : world.getPlayers()) {
                org.bukkit.Location loc = player.getLocation();
                int px = loc.getBlockX();
                int py = loc.getBlockY();
                int pz = loc.getBlockZ();
                int radius = 24;
                int checked = 0;
                for (int x = px - radius; x <= px + radius && checked < 30; x += 4) {
                    for (int z = pz - radius; z <= pz + radius && checked < 30; z += 4) {
                        for (int y = py + 5; y >= py - 5 && checked < 30; y--) {
                            Block block = world.getBlockAt(x, y, z);
                            if (block.getType() == Material.OAK_LEAVES || block.getType() == Material.BIRCH_LEAVES || block.getType() == Material.SPRUCE_LEAVES) {
                                if (random.nextInt(20) == 0) {
                                    block.getWorld().spawnParticle(org.bukkit.Particle.CRIT, block.getLocation().add(0.5, 0.5, 0.5), 2, 0.2, 0.2, 0.2, 0.0);
                                }
                                if (random.nextInt(200) == 0) {
                                    block.setType(Material.AIR);
                                }
                                checked++;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void placeEmberBlocks() {
        for (World world : plugin.getServer().getWorlds()) {
            if (!configManager.getStringList("world.apply-effects-to").contains(world.getName())) {
                continue;
            }
            for (Player player : world.getPlayers()) {
                org.bukkit.Location loc = player.getLocation();
                int px = loc.getBlockX();
                int py = loc.getBlockY();
                int pz = loc.getBlockZ();
                int radius = 20;
                int checked = 0;
                for (int x = px - radius; x <= px + radius && checked < 15; x += 6) {
                    for (int z = pz - radius; z <= pz + radius && checked < 15; z += 6) {
                        for (int y = py - 3; y <= py + 1 && checked < 15; y++) {
                            Block block = world.getBlockAt(x, y, z);
                            if (block.getType() == Material.GRASS_BLOCK || block.getType() == Material.DIRT) {
                                if (random.nextInt(60) == 0) {
                                    Block above = block.getRelative(0, 1, 0);
                                    if (above.getType() == Material.AIR) {
                                        above.setType(Material.FIRE);
                                    }
                                }
                                checked++;
                                break;
                            }
                        }
                    }
                }
            }
        }
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
