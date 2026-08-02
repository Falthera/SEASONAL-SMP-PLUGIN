package io.github.seasonalsmp.seasonalsmp.sword;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class SwordCombatListener implements Listener {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;
    private final java.util.UUID dummyUuid = java.util.UUID.fromString("00000000-0000-0000-0000-000000000000");

    public SwordCombatListener(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (weapon == null || weapon.getType().isAir() || !weapon.hasItemMeta()) {
            return;
        }
        BoundType bound = getSwordBound(weapon);
        if (bound == null) {
            return;
        }
        double damage = event.getDamage();
        Season current = plugin.getSeasonManager().getCurrentSeason();
        if (bound.isPeakSeason(current)) {
            damage *= configManager.getDouble("bound.peak-season-bonus-multiplier", 1.5);
        }
        event.setDamage(damage);
    }

    private BoundType getSwordBound(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "bound_type");
        String value = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (value == null) {
            return null;
        }
        return BoundType.fromString(value);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
    }
}
