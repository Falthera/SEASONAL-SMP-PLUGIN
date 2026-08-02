package io.github.seasonalsmp.seasonalsmp.seasonalblade;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.sword.SwordManager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class LegendaryItemProtectionListener implements Listener {

    private final SeasonalSMP plugin;
    private final SwordManager swordManager;
    private final NamespacedKey seasonalItemKey;
    private final NamespacedKey boundKey;
    private final Set<String> protectedItemKeys;

    public LegendaryItemProtectionListener(SeasonalSMP plugin, SwordManager swordManager) {
        this.plugin = plugin;
        this.swordManager = swordManager;
        this.seasonalItemKey = new NamespacedKey(plugin, "seasonal_item_type");
        this.boundKey = new NamespacedKey(plugin, "bound_type");
        this.protectedItemKeys = new HashSet<>(Arrays.asList(
            "bloom_sword",
            "solstice_blade",
            "harvest_blade",
            "frostreaver",
            "seasonal_blade"
        ));
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (isProtectedItem(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou cannot drop this legendary item!");
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Item itemEntity)) {
            return;
        }
        ItemStack item = itemEntity.getItemStack();
        if (!isProtectedItem(item)) {
            return;
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID
            || event.getCause() == EntityDamageEvent.DamageCause.FIRE
            || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK
            || event.getCause() == EntityDamageEvent.DamageCause.LAVA
            || event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
            event.setCancelled(true);
        }
    }

    private boolean isProtectedItem(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        String seasonalValue = meta.getPersistentDataContainer().get(seasonalItemKey, PersistentDataType.STRING);
        if (seasonalValue != null && protectedItemKeys.contains(seasonalValue)) {
            return true;
        }
        String boundValue = meta.getPersistentDataContainer().get(boundKey, PersistentDataType.STRING);
        if (boundValue != null) {
            try {
                BoundType bound = BoundType.fromString(boundValue);
                if (bound != null) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }
}
