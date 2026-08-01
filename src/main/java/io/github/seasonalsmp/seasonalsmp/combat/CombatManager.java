package io.github.seasonalsmp.seasonalsmp.combat;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CombatManager {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;
    private final Map<UUID, CombatEntry> combatTracker;
    private BukkitTask cleanupTask;

    private static final long COMBAT_DURATION_TICKS = 30L;

    public CombatManager(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.combatTracker = new ConcurrentHashMap<>();
        startCleanupTask();
    }

    public void shutdown() {
        if (cleanupTask != null && !cleanupTask.isCancelled()) {
            cleanupTask.cancel();
        }
        combatTracker.clear();
    }

    public void markInCombat(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        combatTracker.put(player.getUniqueId(), new CombatEntry(System.currentTimeMillis()));
    }

    public boolean isInCombat(Player player) {
        if (player == null) {
            return false;
        }
        CombatEntry entry = combatTracker.get(player.getUniqueId());
        if (entry == null) {
            return false;
        }
        long elapsed = System.currentTimeMillis() - entry.startedAt;
        return elapsed < (COMBAT_DURATION_TICKS * 50L);
    }

    public void removeFromCombat(Player player) {
        if (player == null) {
            return;
        }
        combatTracker.remove(player.getUniqueId());
    }

    private void startCleanupTask() {
        cleanupTask = new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Iterator<Map.Entry<UUID, CombatEntry>> iterator = combatTracker.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<UUID, CombatEntry> entry = iterator.next();
                    if (now - entry.getValue().startedAt >= (COMBAT_DURATION_TICKS * 50L)) {
                        iterator.remove();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public boolean hasTooManyTotems(Player player) {
        if (player == null) return false;
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.TOTEM_OF_UNDYING) {
                count++;
            }
        }
        return count > 1;
    }

    public boolean hasTooManyWolves(Player player) {
        if (player == null) return false;
        int wolfCount = 0;
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), 50, 50, 50)) {
            if (entity instanceof Wolf wolf && wolf.getOwner() != null && wolf.getOwner().getUniqueId().equals(player.getUniqueId())) {
                wolfCount++;
            }
        }
        return wolfCount > 1;
    }

    public boolean hasForbiddenWeapon(Player player) {
        if (player == null) return false;
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        return isForbiddenWeapon(mainHand) || isForbiddenWeapon(offHand);
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

    public boolean exceedsArmorEnchant(Player player) {
        if (player == null) return false;
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null || armor.getType().isAir()) {
                continue;
            }
            int protLevel = armor.getEnchantmentLevel(Enchantment.PROTECTION);
            if (protLevel > 3) {
                return true;
            }
        }
        return false;
    }

    public boolean exceedsWeaponEnchant(Player player) {
        if (player == null) return false;
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand == null || mainHand.getType().isAir()) {
            return false;
        }
        return mainHand.getEnchantmentLevel(Enchantment.SHARPNESS) > 4;
    }

    public boolean hasIllegalPickaxe(Player player) {
        if (player == null) return false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir()) {
                continue;
            }
            if (item.getType().toString().endsWith("_PICKAXE") && item.getType() != Material.NETHERITE_PICKAXE) {
                return true;
            }
        }
        return false;
    }

    public boolean hasDebuffProjectile(Player player) {
        if (player == null) return false;
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        return hasDebuffProjectile(mainHand) || hasDebuffProjectile(offHand);
    }

    private boolean hasDebuffProjectile(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        if (item.getType() == Material.TIPPED_ARROW || item.getType() == Material.LINGERING_POTION || item.getType() == Material.SPLASH_POTION) {
            return true;
        }
        return false;
    }

    public boolean isBedBombing(Player player, Block block) {
        if (player == null || block == null) {
            return false;
        }
        Material type = block.getType();
        return type == Material.RESPAWN_ANCHOR;
    }

    public boolean isEnderPearlEscape(Player player) {
        if (player == null || !isInCombat(player)) {
            return false;
        }
        return player.getInventory().getItemInMainHand().getType() == Material.ENDER_PEARL ||
               player.getInventory().getItemInOffHand().getType() == Material.ENDER_PEARL;
    }

    public boolean isWaterRunning(Player player) {
        if (player == null || !isInCombat(player)) {
            return false;
        }
        if (player.isInWater()) {
            Location loc = player.getLocation();
            if (loc.getBlock().getType() == Material.WATER) {
                return true;
            }
        }
        return false;
    }

    public boolean exceedsRestockLimit(Player player, Material material) {
        if (player == null) {
            return false;
        }
        int total = countMaterial(player, material);
        return switch (material) {
            case EXPERIENCE_BOTTLE -> total > 128;
            case BREEZE_ROD -> total > 64;
            case WIND_CHARGE -> total > 128;
            case GOLDEN_APPLE -> total > 128;
            case COBWEB -> total > 128;
            default -> false;
        };
    }

    private int countMaterial(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private static class CombatEntry {
        final long startedAt;

        CombatEntry(long startedAt) {
            this.startedAt = startedAt;
        }
    }
}
