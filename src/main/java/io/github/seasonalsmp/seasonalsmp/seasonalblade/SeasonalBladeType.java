package io.github.seasonalsmp.seasonalsmp.seasonalblade;

import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public enum SeasonalBladeType {

    BLOOM_SWORD("bloom_sword", BoundType.SPRING, 1),
    SOLSTICE_BLADE("solstice_blade", BoundType.SUMMER, 5),
    HARVEST_BLADE("harvest_blade", BoundType.AUTUMN, 3),
    FROSTREAVER("frostreaver", BoundType.WINTER, 7),
    SEASONAL_BLADE("seasonal_blade", null, -1);

    private final String key;
    private final BoundType boundType;
    private final int craftingSlot;
    private final NamespacedKey pdcKey;

    SeasonalBladeType(String key, BoundType boundType, int craftingSlot) {
        this.key = key;
        this.boundType = boundType;
        this.craftingSlot = craftingSlot;
        this.pdcKey = new NamespacedKey(io.github.seasonalsmp.seasonalsmp.SeasonalSMP.get(), "seasonal_item_type");
    }

    public String getKey() {
        return key;
    }

    public BoundType getBoundType() {
        return boundType;
    }

    public int getCraftingSlot() {
        return craftingSlot;
    }

    public NamespacedKey getPdcKey() {
        return pdcKey;
    }

    public static SeasonalBladeType fromItem(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        String value = meta.getPersistentDataContainer().get(
            new NamespacedKey(io.github.seasonalsmp.seasonalsmp.SeasonalSMP.get(), "seasonal_item_type"),
            PersistentDataType.STRING
        );
        if (value == null) {
            return null;
        }
        return switch (value) {
            case "bloom_sword" -> BLOOM_SWORD;
            case "solstice_blade" -> SOLSTICE_BLADE;
            case "harvest_blade" -> HARVEST_BLADE;
            case "frostreaver" -> FROSTREAVER;
            case "seasonal_blade" -> SEASONAL_BLADE;
            default -> null;
        };
    }

    public static boolean isSeasonalSword(ItemStack item) {
        SeasonalBladeType type = fromItem(item);
        return type != null && type != SEASONAL_BLADE;
    }

    public static boolean isSeasonalBlade(ItemStack item) {
        SeasonalBladeType type = fromItem(item);
        return type == SEASONAL_BLADE;
    }
}
