package io.github.seasonalsmp.seasonalsmp.combat;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CombatListener implements Listener {

    private final SeasonalSMP plugin;
    private final CombatManager combatManager;

    public CombatListener(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.combatManager = plugin.getCombatManager();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victim && event.getDamager() instanceof Player attacker) {
            combatManager.markInCombat(victim);
            combatManager.markInCombat(attacker);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        combatManager.removeFromCombat(event.getEntity());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (combatManager.isInCombat(player)) {
            Bukkit.broadcastMessage("§c§l" + player.getName() + " §cis suspected of combat logging!");
        }
        combatManager.removeFromCombat(player);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!combatManager.isInCombat(player)) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir()) {
            return;
        }

        if (item.getType() == Material.ENDER_PEARL) {
            event.setCancelled(true);
            player.sendMessage("§cYou cannot use Ender Pearls while in combat!");
            return;
        }

        if (isForbiddenWeapon(item)) {
            event.setCancelled(true);
            player.sendMessage("§cThis weapon is not allowed in combat!");
            return;
        }

        if (isBombingMaterial(item)) {
            event.setCancelled(true);
            player.sendMessage("§cThis item is not allowed in PvP combat!");
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (!combatManager.isInCombat(player)) {
            return;
        }
        if (event.getRightClicked() instanceof Wolf wolf) {
            if (combatManager.hasTooManyWolves(player)) {
                event.setCancelled(true);
                player.sendMessage("§cYou may only have 1 wolf during combat!");
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!combatManager.isInCombat(player)) {
            return;
        }
        if (combatManager.isWaterRunning(player)) {
            event.setCancelled(true);
            player.sendMessage("§cExtended water running is not allowed during combat!");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!combatManager.isInCombat(player)) {
            return;
        }
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType().isAir()) {
            return;
        }
        if (combatManager.hasForbiddenWeapon(player) || combatManager.exceedsArmorEnchant(player) || combatManager.exceedsWeaponEnchant(player)) {
            event.setCancelled(true);
            player.sendMessage("§cYour loadout violates combat rules!");
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!combatManager.isInCombat(player)) {
            return;
        }
        if (combatManager.hasForbiddenWeapon(player) || combatManager.exceedsArmorEnchant(player) || combatManager.exceedsWeaponEnchant(player)) {
            event.setCancelled(true);
            player.sendMessage("§cYour loadout violates combat rules!");
        }
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (!combatManager.isInCombat(player)) {
            return;
        }
        if (event.getBucket() == Material.WATER_BUCKET || event.getBucket() == Material.LAVA_BUCKET) {
            event.setCancelled(true);
            player.sendMessage("§cDraining by bucket is not allowed during combat!");
        }
    }

    private boolean isForbiddenWeapon(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        Material type = item.getType();

        if (type == Material.BOW) {
            return item.getEnchantmentLevel(Enchantment.PUNCH) > 0;
        }

        if (type == Material.TNT || type == Material.TNT_MINECART) {
            return true;
        }

        if (type == Material.END_CRYSTAL || type == Material.RESPAWN_ANCHOR) {
            return true;
        }

        if (type == Material.MACE) {
            return item.getEnchantmentLevel(Enchantment.DENSITY) > 4;
        }

        if (type == Material.TRIDENT) {
            return true;
        }

        if (type == Material.CROSSBOW) {
            return true;
        }

        return false;
    }

    private boolean isBombingMaterial(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        Material type = item.getType();
        return type == Material.RESPAWN_ANCHOR || type == Material.END_CRYSTAL;
    }
}
