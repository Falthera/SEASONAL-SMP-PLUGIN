package io.github.seasonalsmp.seasonalsmp.seasonalblade;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.*;

public class SeasonalBladeListener implements Listener {

    private final SeasonalSMP plugin;
    private final SeasonalBladeManager bladeManager;

    public SeasonalBladeListener(SeasonalSMP plugin, SeasonalBladeManager bladeManager) {
        this.plugin = plugin;
        this.bladeManager = bladeManager;
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        ItemStack[] matrix = inventory.getMatrix();
        if (!isValidSeasonalBladeRecipe(matrix)) {
            event.getInventory().setResult(new ItemStack(Material.AIR));
            return;
        }
        ItemStack result = bladeManager.buildSeasonalBlade();
        if (result == null) {
            event.getInventory().setResult(new ItemStack(Material.AIR));
            return;
        }
        event.getInventory().setResult(result);
    }

    private boolean isValidSeasonalBladeRecipe(ItemStack[] matrix) {
        if (matrix == null || matrix.length != 9) {
            return false;
        }
        boolean hasMace = false;
        for (int i = 0; i < matrix.length; i++) {
            ItemStack item = matrix[i];
            if (item == null || item.getType().isAir()) {
                continue;
            }
            if (item.getType() == Material.MACE) {
                hasMace = true;
                continue;
            }
            if (!SeasonalBladeType.isSeasonalSword(item)) {
                return false;
            }
        }
        if (!hasMace) {
            return false;
        }
        if (!isItemOfType(matrix, 1, SeasonalBladeType.BLOOM_SWORD)) {
            return false;
        }
        if (!isItemOfType(matrix, 3, SeasonalBladeType.HARVEST_BLADE)) {
            return false;
        }
        if (!isItemOfType(matrix, 4, null)) {
            return false;
        }
        if (!isItemOfType(matrix, 5, SeasonalBladeType.SOLSTICE_BLADE)) {
            return false;
        }
        if (!isItemOfType(matrix, 7, SeasonalBladeType.FROSTREAVER)) {
            return false;
        }
        return true;
    }

    private boolean isItemOfType(ItemStack[] matrix, int slot, SeasonalBladeType expectedType) {
        if (slot < 0 || slot >= matrix.length) {
            return false;
        }
        ItemStack item = matrix[slot];
        if (item == null || item.getType().isAir()) {
            return expectedType == null;
        }
        if (expectedType == null) {
            return false;
        }
        SeasonalBladeType type = SeasonalBladeType.fromItem(item);
        return type == expectedType;
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getView().getPlayer() instanceof Player player)) {
            return;
        }
        org.bukkit.inventory.Recipe recipe = event.getRecipe();
        if (recipe instanceof org.bukkit.inventory.ShapedRecipe shaped && "seasonal_blade".equals(shaped.getKey().getKey())) {
            CraftingInventory inventory = event.getInventory();
            ItemStack[] matrix = inventory.getMatrix();
            if (!isValidSeasonalBladeRecipe(matrix)) {
                event.setCancelled(true);
                event.getInventory().setResult(new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
                return;
            }
            if (!bladeManager.canCraftSeasonalBlade()) {
                event.setCancelled(true);
                event.getInventory().setResult(new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
                player.sendMessage("§cThe Seasonal Blade has already been forged on this server!");
                return;
            }
            event.setCancelled(true);
            ItemStack result = bladeManager.buildSeasonalBlade();
            if (result == null) {
                event.setCancelled(true);
                event.getInventory().setResult(new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
                player.sendMessage("§cThe Seasonal Blade has already been forged on this server!");
                return;
            }
            bladeManager.markSeasonalBladeCrafted();
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(result);
            if (!leftover.isEmpty()) {
                player.sendMessage("§c§lYour inventory is full! Drop something to make room for the Seasonal Blade!");
                for (ItemStack drop : leftover.values()) {
                    org.bukkit.entity.Item entity = player.getWorld().dropItemNaturally(player.getLocation(), drop);
                    entity.setPickupDelay(0);
                    entity.setTicksLived(1);
                }
            }
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 5.0f, 0.5f);
            player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 1.0f);
            for (int i = 0; i < 50; i++) {
                double x = (Math.random() - 0.5) * 10;
                double y = Math.random() * 5;
                double z = (Math.random() - 0.5) * 10;
                player.spawnParticle(org.bukkit.Particle.FIREWORK, player.getLocation().add(x, y, z), 10, 0.2, 0.2, 0.2, 0.1);
            }
            Bukkit.broadcastMessage("§6§l" + player.getName() + " §r§6has forged the legendary SEASONAL BLADE!");
            consumeItem(matrix, 1);
            consumeItem(matrix, 3);
            consumeItem(matrix, 4);
            consumeItem(matrix, 5);
            consumeItem(matrix, 7);
        }
    }

    private void consumeItem(ItemStack[] matrix, int slot) {
        if (slot < 0 || slot >= matrix.length) {
            return;
        }
        ItemStack item = matrix[slot];
        if (item == null || item.getType().isAir()) {
            return;
        }
        if (item.getAmount() <= 1) {
            matrix[slot] = new ItemStack(Material.AIR);
        } else {
            item.setAmount(item.getAmount() - 1);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        if (!bladeManager.isSeasonalBlade(player.getInventory().getItemInMainHand())) {
            return;
        }
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        event.setCancelled(true);
        bladeManager.activateAbility(player);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        bladeManager.onEntityDamageByEntity(event);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        bladeManager.onPlayerMove(event);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        bladeManager.stopPassiveEffects(event.getPlayer());
    }
}

