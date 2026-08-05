package io.github.seasonalsmp.seasonalsmp.combat;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EnchantmentLimiterListener implements Listener {

    private final SeasonalSMP plugin;

    public EnchantmentLimiterListener(SeasonalSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        int maxSharpness = plugin.getConfigManager().getInt("combat.max-sharpness-level", 4);
        int maxProtection = plugin.getConfigManager().getInt("combat.max-protection-level", 3);
        int maxMaceDensity = plugin.getConfigManager().getInt("combat.max-mace-density", 1);

        java.util.Map<Enchantment, Integer> enchantsToAdd = event.getEnchantsToAdd();
        boolean modified = false;
        for (java.util.Map.Entry<Enchantment, Integer> entry : enchantsToAdd.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();
            int limit = getEnchantmentLimit(enchantment, maxSharpness, maxProtection, maxMaceDensity);
            if (limit >= 0 && level > limit) {
                enchantsToAdd.put(enchantment, limit);
                modified = true;
            }
        }

        if (modified) {
            Player player = (Player) event.getEnchanter();
            player.sendMessage("§cEnchantment levels have been capped by server rules!");
        }
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        int maxSharpness = plugin.getConfigManager().getInt("combat.max-sharpness-level", 4);
        int maxProtection = plugin.getConfigManager().getInt("combat.max-protection-level", 3);
        int maxMaceDensity = plugin.getConfigManager().getInt("combat.max-mace-density", 1);

        ItemStack result = event.getResult();
        if (result == null || result.getType().isAir() || !result.hasItemMeta()) {
            return;
        }

        ItemMeta meta = result.getItemMeta();
        if (meta == null) {
            return;
        }

        boolean modified = false;
        for (Enchantment enchantment : meta.getEnchants().keySet()) {
            int level = meta.getEnchantmentLevel(enchantment);
            int limit = getEnchantmentLimit(enchantment, maxSharpness, maxProtection, maxMaceDensity);
            if (limit >= 0 && level > limit) {
                meta.removeEnchant(enchantment);
                meta.addEnchant(enchantment, limit, true);
                modified = true;
            }
        }

        if (modified) {
            result.setItemMeta(meta);
            event.setResult(result);
            Player player = (Player) event.getView().getPlayer();
            player.sendMessage("§cEnchantment levels have been capped by server rules!");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.GRINDSTONE) {
            return;
        }

        ItemStack result = event.getCurrentItem();
        if (result == null || result.getType().isAir() || !result.hasItemMeta()) {
            return;
        }

        int maxSharpness = plugin.getConfigManager().getInt("combat.max-sharpness-level", 4);
        int maxProtection = plugin.getConfigManager().getInt("combat.max-protection-level", 3);
        int maxMaceDensity = plugin.getConfigManager().getInt("combat.max-mace-density", 1);

        ItemMeta meta = result.getItemMeta();
        if (meta == null) {
            return;
        }

        boolean modified = false;
        for (Enchantment enchantment : meta.getEnchants().keySet()) {
            int level = meta.getEnchantmentLevel(enchantment);
            int limit = getEnchantmentLimit(enchantment, maxSharpness, maxProtection, maxMaceDensity);
            if (limit >= 0 && level > limit) {
                meta.removeEnchant(enchantment);
                meta.addEnchant(enchantment, limit, true);
                modified = true;
            }
        }

        if (modified) {
            result.setItemMeta(meta);
            Player player = (Player) event.getWhoClicked();
            player.sendMessage("§cEnchantment levels have been capped by server rules!");
        }
    }

    private int getEnchantmentLimit(Enchantment enchantment, int maxSharpness, int maxProtection, int maxMaceDensity) {
        if (enchantment == Enchantment.SHARPNESS || enchantment == Enchantment.SMITE || enchantment == Enchantment.BANE_OF_ARTHROPODS) {
            return maxSharpness;
        }
        if (enchantment == Enchantment.PROTECTION
                || enchantment == Enchantment.FIRE_PROTECTION
                || enchantment == Enchantment.BLAST_PROTECTION
                || enchantment == Enchantment.PROJECTILE_PROTECTION) {
            return maxProtection;
        }
        if (enchantment == Enchantment.DENSITY) {
            return maxMaceDensity;
        }
        return -1;
    }
}
