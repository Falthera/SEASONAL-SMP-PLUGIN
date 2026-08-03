package io.github.seasonalsmp.seasonalsmp.grace;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

public class GracePeriodListener implements Listener {

    private final SeasonalSMP plugin;
    private final GracePeriodManager gracePeriodManager;

    public GracePeriodListener(SeasonalSMP plugin, GracePeriodManager gracePeriodManager) {
        this.plugin = plugin;
        this.gracePeriodManager = gracePeriodManager;
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!gracePeriodManager.isActive()) {
            return;
        }
        org.bukkit.inventory.Recipe recipe = event.getRecipe();
        if (recipe instanceof org.bukkit.inventory.ShapedRecipe shaped) {
            String key = shaped.getKey().getKey();
            if (key.equals("spring_sword") || key.equals("summer_sword") ||
                key.equals("autumn_sword") || key.equals("winter_sword") ||
                key.equals("seasonal_blade")) {
                event.setCancelled(true);
                if (event.getView().getPlayer() instanceof Player player) {
                    player.sendMessage("§cSword crafting is disabled during the grace period!");
                    player.closeInventory();
                }
            }
        }
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (!gracePeriodManager.isActive()) {
            return;
        }
        org.bukkit.inventory.Recipe recipe = event.getRecipe();
        if (recipe instanceof org.bukkit.inventory.ShapedRecipe shaped) {
            String key = shaped.getKey().getKey();
            if (key.equals("spring_sword") || key.equals("summer_sword") ||
                key.equals("autumn_sword") || key.equals("winter_sword") ||
                key.equals("seasonal_blade")) {
                event.getInventory().setResult(new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
            }
        }
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
