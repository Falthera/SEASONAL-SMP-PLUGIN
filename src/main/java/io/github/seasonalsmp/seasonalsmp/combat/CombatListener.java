package io.github.seasonalsmp.seasonalsmp.combat;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.seasonalblade.SeasonalBladeType;
import org.bukkit.Bukkit;
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
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CombatListener implements Listener {

    private final SeasonalSMP plugin;
    private final CombatManager combatManager;

    public CombatListener(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.combatManager = plugin.getCombatManager();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isEnabled()) {
                    cancel();
                    return;
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    downgradeExcessiveSharpness(player);
                    downgradeExcessiveProtection(player);
                }
            }
        }.runTaskTimer(plugin, 200L, 200L);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (plugin.getGracePeriodManager().isActive()) {
                event.setCancelled(true);
                return;
            }
            downgradeExcessiveSharpness(attacker);
        }
        if (event.getEntity() instanceof Player victim && event.getDamager() instanceof Player attacker) {
            downgradeExcessiveProtection(victim);
            combatManager.markInCombat(victim);
            combatManager.markInCombat(attacker);
            if (plugin.getTrustManager().isTrusted(attacker, victim) || plugin.getTrustManager().isTrusted(victim, attacker)) {
                event.setCancelled(true);
                attacker.sendMessage("§cYou cannot harm a trusted ally.");
            }
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
    public void onPlayerJoin(PlayerJoinEvent event) {
        downgradeExcessiveProtection(event.getPlayer());
        downgradeExcessiveSharpness(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        downgradeExcessiveProtection(event.getPlayer());
        downgradeExcessiveSharpness(event.getPlayer());
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
            player.sendMessage("§cEnder Pearls are on cooldown until you leave combat!");
            return;
        }

        if (isForbiddenWeapon(item)) {
            event.setCancelled(true);
            player.sendMessage("§cThis weapon is on cooldown until you leave combat!");
            return;
        }

        if (isBombingMaterial(item)) {
            event.setCancelled(true);
            player.sendMessage("§cThis item is on cooldown until you leave combat!");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack current = event.getCurrentItem();
        if (current != null && !current.getType().isAir() && current.hasItemMeta()) {
            downgradeExcessiveSharpness(player);
            downgradeExcessiveProtection(player);
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
            int maxMaceDensity = plugin.getConfigManager().getInt("combat.max-mace-density", 1);
            if (SeasonalBladeType.isSeasonalBlade(item)) {
                return item.getEnchantmentLevel(Enchantment.DENSITY) > 2
                    || item.getEnchantmentLevel(Enchantment.WIND_BURST) > 1
                    || item.getEnchantmentLevel(Enchantment.BREACH) > 2;
            }
            return item.getEnchantmentLevel(Enchantment.DENSITY) > maxMaceDensity;
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

    private void downgradeExcessiveSharpness(Player player) {
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null || weapon.getType().isAir() || !weapon.hasItemMeta()) {
            return;
        }
        int maxSharpness = plugin.getConfigManager().getInt("combat.max-sharpness-level", 4);
        int level = weapon.getEnchantmentLevel(Enchantment.SHARPNESS);
        if (level > maxSharpness) {
            ItemMeta meta = weapon.getItemMeta();
            if (meta != null) {
                meta.removeEnchant(Enchantment.SHARPNESS);
                meta.addEnchant(Enchantment.SHARPNESS, maxSharpness, true);
                weapon.setItemMeta(meta);
                player.sendMessage("§cYour weapon's Sharpness has been capped to " + maxSharpness + "!");
            }
        }
    }

    private void downgradeExcessiveProtection(Player player) {
        int maxProtection = plugin.getConfigManager().getInt("combat.max-protection-level", 3);
        boolean downgraded = false;
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
                continue;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                continue;
            }
            boolean modified = false;
            for (Enchantment enchantment : meta.getEnchants().keySet()) {
                if (enchantment == Enchantment.PROTECTION
                        || enchantment == Enchantment.FIRE_PROTECTION
                        || enchantment == Enchantment.BLAST_PROTECTION
                        || enchantment == Enchantment.PROJECTILE_PROTECTION) {
                    int currentLevel = meta.getEnchantmentLevel(enchantment);
                    if (currentLevel > maxProtection) {
                        meta.removeEnchant(enchantment);
                        meta.addEnchant(enchantment, maxProtection, true);
                        modified = true;
                    }
                }
            }
            if (modified) {
                item.setItemMeta(meta);
                downgraded = true;
            }
        }
        if (downgraded) {
            player.sendMessage("§cYour armor's Protection has been capped to " + maxProtection + "!");
        }
    }
}
