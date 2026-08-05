package io.github.seasonalsmp.seasonalsmp.listener;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

public class StaffModeListener implements Listener {

    private final SeasonalSMP plugin;

    public StaffModeListener(SeasonalSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) {
            return;
        }
        if (!player.hasPermission("seasonalsmp.command.warn")) {
            return;
        }
        if (event.getNewGameMode() == GameMode.SPECTATOR || event.getNewGameMode() == GameMode.CREATIVE) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage("§cYou can only use §fSpectator §cor §fCreative §cgamemode.");
    }
}
