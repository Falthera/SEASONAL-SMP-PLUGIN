package io.github.seasonalsmp.seasonalsmp.seasonalblade;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.regex.Pattern;

public class SpearBlockerListener implements Listener {

    private final SeasonalSMP plugin;
    private final Pattern spearPattern = Pattern.compile(".*_spear", Pattern.CASE_INSENSITIVE);

    private static final Set<String> BLOCKED_SPEAR_KEYS = Set.of(
        "minecraft:wooden_spear",
        "minecraft:stone_spear",
        "minecraft:iron_spear",
        "minecraft:diamond_spear",
        "minecraft:netherite_spear"
    );

    public SpearBlockerListener(SeasonalSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        if (isBlockedSpear(item)) {
            event.setCancelled(true);
            event.getItem().remove();
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();
        if (isBlockedSpear(cursor) || isBlockedSpear(current)) {
            event.setCancelled(true);
            player.sendMessage("§cSpears are not allowed on this server!");
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        for (ItemStack item : event.getNewItems().values()) {
            if (isBlockedSpear(item)) {
                event.setCancelled(true);
                player.sendMessage("§cSpears are not allowed on this server!");
                return;
            }
        }
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        ItemStack result = event.getInventory().getResult();
        if (isBlockedSpear(result)) {
            event.getInventory().setResult(new ItemStack(Material.AIR));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        removeSpearsFromPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        removeSpearsFromPlayer(event.getPlayer());
    }

    private void removeSpearsFromPlayer(Player player) {
        boolean removed = false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (isBlockedSpear(item)) {
                player.getInventory().remove(item);
                removed = true;
            }
        }
        if (removed) {
            player.sendMessage("§cBlocked spears were removed from your inventory!");
        }
    }

    public boolean isBlockedSpear(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        String key = meta.getPersistentDataContainer().get(
            new NamespacedKey(plugin, "seasonal_item_type"),
            org.bukkit.persistence.PersistentDataType.STRING
        );
        if (key != null && spearPattern.matcher(key).matches()) {
            return true;
        }

        if (meta.hasDisplayName() && spearPattern.matcher(meta.getDisplayName()).matches()) {
            return true;
        }

        String materialName = item.getType().name();
        if (spearPattern.matcher(materialName).matches()) {
            return true;
        }

        if (BLOCKED_SPEAR_KEYS.contains(item.getType().getKey().toString())) {
            return true;
        }

        return false;
    }
}
