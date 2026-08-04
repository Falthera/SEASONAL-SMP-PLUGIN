package io.github.seasonalsmp.seasonalsmp.listener;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;

    public PlayerJoinListener(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.getConfigManager().getBoolean("bound.assign-on-first-join")) {
            if (!plugin.getBoundManager().hasBound(player)) {
                plugin.getBoundManager().assignRandomBound(player);
            }
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getUIManager().updateBossBar(player, plugin.getSeasonManager().getCurrentSeason());
            if (plugin.getSeasonalBladeManager().isSeasonalBlade(player.getInventory().getItemInMainHand())) {
                plugin.getSeasonalBladeManager().startPassiveEffects(player);
            }
        }, 40L);
    }

    @EventHandler
    public void onPlayerRespawn(org.bukkit.event.player.PlayerRespawnEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getSeasonalBladeManager().isSeasonalBlade(event.getPlayer().getInventory().getItemInMainHand())) {
                plugin.getSeasonalBladeManager().startPassiveEffects(event.getPlayer());
            }
        }, 40L);
    }
}
