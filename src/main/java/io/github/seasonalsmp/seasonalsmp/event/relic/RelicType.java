package io.github.seasonalsmp.seasonalsmp.event.relic;

import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public enum RelicType {

    SPRING_RELIC("Relic of Bloom", BoundType.SPRING, Material.TOTEM_OF_UNDYING, 2001),
    SUMMER_RELIC("Relic of Solstice", BoundType.SUMMER, Material.BLAZE_ROD, 2002),
    AUTUMN_RELIC("Relic of Harvest", BoundType.AUTUMN, Material.NETHER_STAR, 2003),
    WINTER_RELIC("Relic of Frost", BoundType.WINTER, Material.HEART_OF_THE_SEA, 2004),
    BLOODBORN_RELIC("Bloodborn Relic", null, Material.NETHERITE_INGOT, 2005);

    private final String displayName;
    private final BoundType boundType;
    private final Material material;
    private final int customModelData;

    RelicType(String displayName, BoundType boundType, Material material, int customModelData) {
        this.displayName = displayName;
        this.boundType = boundType;
        this.material = material;
        this.customModelData = customModelData;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BoundType getBoundType() {
        return boundType;
    }

    public Material getMaterial() {
        return material;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public static RelicType fromBound(BoundType bound) {
        if (bound == null) return null;
        return switch (bound) {
            case SPRING -> SPRING_RELIC;
            case SUMMER -> SUMMER_RELIC;
            case AUTUMN -> AUTUMN_RELIC;
            case WINTER -> WINTER_RELIC;
        };
    }

    public ItemStack createItem() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        String color = switch (this) {
            case SPRING_RELIC -> "&a";
            case SUMMER_RELIC -> "&e";
            case AUTUMN_RELIC -> "&6";
            case WINTER_RELIC -> "&b";
            case BLOODBORN_RELIC -> "&4";
        };
        meta.setDisplayName(color + "&l" + displayName);
        meta.setCustomModelData(customModelData);
        meta.getPersistentDataContainer().set(
            new NamespacedKey(io.github.seasonalsmp.seasonalsmp.SeasonalSMP.get(), "relic_type"),
            PersistentDataType.STRING,
            name()
        );
        item.setItemMeta(meta);
        return item;
    }

    public static RelicType fromItem(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        String value = meta.getPersistentDataContainer().get(
            new NamespacedKey(io.github.seasonalsmp.seasonalsmp.SeasonalSMP.get(), "relic_type"),
            PersistentDataType.STRING
        );
        if (value == null) {
            return null;
        }
        return switch (value) {
            case "SPRING_RELIC" -> SPRING_RELIC;
            case "SUMMER_RELIC" -> SUMMER_RELIC;
            case "AUTUMN_RELIC" -> AUTUMN_RELIC;
            case "WINTER_RELIC" -> WINTER_RELIC;
            case "BLOODBORN_RELIC" -> BLOODBORN_RELIC;
            default -> null;
        };
    }
}
