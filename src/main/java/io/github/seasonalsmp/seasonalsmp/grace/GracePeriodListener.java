package io.github.seasonalsmp.seasonalsmp.grace;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class GracePeriodListener implements Listener {

    private final SeasonalSMP plugin;
    private final GracePeriodManager gracePeriodManager;

    public GracePeriodListener(SeasonalSMP plugin, GracePeriodManager gracePeriodManager) {
        this.plugin = plugin;
        this.gracePeriodManager = gracePeriodManager;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!gracePeriodManager.isActive()) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!gracePeriodManager.isActive()) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }
}
