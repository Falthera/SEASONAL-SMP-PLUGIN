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
        modified |= capEnchantment(enchantsToAdd, Enchantment.SHARPNESS, maxSharpness);
        modified |= capEnchantment(enchantsToAdd, Enchantment.SMITE, maxSharpness);
        modified |= capEnchantment(enchantsToAdd, Enchantment.BANE_OF_ARTHROPODS, maxSharpness);
        modified |= capEnchantment(enchantsToAdd, Enchantment.PROTECTION, maxProtection);
        modified |= capEnchantment(enchantsToAdd, Enchantment.FIRE_PROTECTION, maxProtection);
        modified |= capEnchantment(enchantsToAdd, Enchantment.BLAST_PROTECTION, maxProtection);
        modified |= capEnchantment(enchantsToAdd, Enchantment.PROJECTILE_PROTECTION, maxProtection);
        modified |= capEnchantment(enchantsToAdd, Enchantment.DENSITY, maxMaceDensity);

        if (modified) {
            Player player = (Player) event.getEnchanter();
            player.sendMessage("§cEnchantment levels have been capped by server rules!");
        }
    }

    private boolean capEnchantment(java.util.Map<Enchantment, Integer> map, Enchantment enchantment, int limit) {
        Integer level = map.get(enchantment);
        if (level != null && level > limit) {
            map.put(enchantment, limit);
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        int maxSharpness = plugin.getConfigManager().getInt("combat.max-sharpness-level", 4);
        int maxProtection = plugin.getConfigManager().getInt("combat.max-protection-level", 3);
        int maxMaceDensity = plugin.getConfigManager().getInt("combat.max-mace-density", 1);

        ItemStack result = event.getResult();
        if (result == null || result.getType().isAir()) {
            return;
        }

        boolean modified = false;
        modified |= limitEnchantment(result, Enchantment.SHARPNESS, maxSharpness);
        modified |= limitEnchantment(result, Enchantment.SMITE, maxSharpness);
        modified |= limitEnchantment(result, Enchantment.BANE_OF_ARTHROPODS, maxSharpness);
        modified |= limitEnchantment(result, Enchantment.PROTECTION, maxProtection);
        modified |= limitEnchantment(result, Enchantment.FIRE_PROTECTION, maxProtection);
        modified |= limitEnchantment(result, Enchantment.BLAST_PROTECTION, maxProtection);
        modified |= limitEnchantment(result, Enchantment.PROJECTILE_PROTECTION, maxProtection);
        modified |= limitEnchantment(result, Enchantment.DENSITY, maxMaceDensity);

        if (modified) {
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
        if (result == null || result.getType().isAir()) {
            return;
        }

        int maxSharpness = plugin.getConfigManager().getInt("combat.max-sharpness-level", 4);
        int maxProtection = plugin.getConfigManager().getInt("combat.max-protection-level", 3);
        int maxMaceDensity = plugin.getConfigManager().getInt("combat.max-mace-density", 1);

        boolean modified = false;
        modified |= limitEnchantment(result, Enchantment.SHARPNESS, maxSharpness);
        modified |= limitEnchantment(result, Enchantment.SMITE, maxSharpness);
        modified |= limitEnchantment(result, Enchantment.BANE_OF_ARTHROPODS, maxSharpness);
        modified |= limitEnchantment(result, Enchantment.PROTECTION, maxProtection);
        modified |= limitEnchantment(result, Enchantment.FIRE_PROTECTION, maxProtection);
        modified |= limitEnchantment(result, Enchantment.BLAST_PROTECTION, maxProtection);
        modified |= limitEnchantment(result, Enchantment.PROJECTILE_PROTECTION, maxProtection);
        modified |= limitEnchantment(result, Enchantment.DENSITY, maxMaceDensity);

        if (modified) {
            Player player = (Player) event.getWhoClicked();
            player.sendMessage("§cEnchantment levels have been capped by server rules!");
        }
    }

    private boolean limitEnchantment(ItemStack item, Enchantment enchantment, int limit) {
        int level = item.getEnchantmentLevel(enchantment);
        if (level > limit) {
            item.removeEnchantment(enchantment);
            item.addEnchantment(enchantment, limit);
            return true;
        }
        return false;
    }
}
