package io.github.seasonalsmp.seasonalsmp.listener;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SeasonWorldListener implements Listener {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;

    public SeasonWorldListener(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        Season current = plugin.getSeasonManager().getCurrentSeason();
        if (current == Season.WINTER) {
            if (!event.toWeatherState()) {
                event.setCancelled(true);
                event.getWorld().setStorm(true);
            }
        } else if (current == Season.SUMMER) {
            if (event.toWeatherState()) {
                event.setCancelled(true);
                event.getWorld().setStorm(false);
            }
        }
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        Season current = plugin.getSeasonManager().getCurrentSeason();
        if (current == Season.WINTER) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        Season current = plugin.getSeasonManager().getCurrentSeason();
        if (current == Season.AUTUMN) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            World world = event.getBlock().getWorld();
            if (world != null) {
                world.spawnParticle(org.bukkit.Particle.CRIT, event.getBlock().getLocation().add(0.5, 0.5, 0.5), 8, 0.3, 0.3, 0.3, 0.1);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Season current = plugin.getSeasonManager().getCurrentSeason();
        if (current == Season.AUTUMN && configManager.getBoolean("world-transformation.extra-loot-enabled")) {
            Block block = event.getBlock();
            if (block.getBlockData() instanceof Ageable ageable && ageable.getAge() == ageable.getMaximumAge()) {
                Material cropType = block.getType();
                Map<Material, Integer> boosts = new HashMap<>();
                boosts.put(Material.WHEAT, 3);
                boosts.put(Material.CARROTS, 3);
                boosts.put(Material.POTATOES, 3);
                boosts.put(Material.BEETROOTS, 3);
                boosts.put(Material.NETHER_WART, 2);
                Integer multiplier = boosts.get(cropType);
                if (multiplier != null && multiplier > 1) {
                    event.setCancelled(true);
                    Collection<ItemStack> drops = block.getDrops(event.getPlayer().getInventory().getItemInMainHand());
                    block.setType(Material.AIR);
                    for (ItemStack drop : drops) {
                        drop.setAmount(drop.getAmount() * multiplier);
                        block.getWorld().dropItemNaturally(block.getLocation(), drop);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() == null) {
            return;
        }
        Season current = plugin.getSeasonManager().getCurrentSeason();
        if (current == Season.SUMMER && configManager.getBoolean("world-transformation.extra-hostile-spawns")) {
            if (event.getEntity() instanceof LivingEntity living) {
                living.setHealth(living.getHealth() * 1.2);
            }
        }
    }
}

