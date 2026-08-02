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
        double baseDamage = getBaseDamage(bound);
        double finalDamage = baseDamage;
        Season current = plugin.getSeasonManager().getCurrentSeason();
        if (bound.isPeakSeason(current)) {
            finalDamage *= configManager.getDouble("bound.peak-season-bonus-multiplier", 1.5);
        }
        event.setDamage(finalDamage);
    }

    private double getBaseDamage(BoundType bound) {
        return switch (bound) {
            case SPRING -> configManager.getDouble("swords.spring-sword.damage-base", 7.0);
            case SUMMER -> configManager.getDouble("swords.summer-sword.damage-base", 8.0);
            case AUTUMN -> configManager.getDouble("swords.autumn-sword.damage-base", 6.5);
            case WINTER -> configManager.getDouble("swords.winter-sword.damage-base", 7.5);
        };
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
