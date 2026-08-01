package io.github.seasonalsmp.seasonalsmp.season;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.*;

public enum Season {

    SPRING("Spring", "&a", 0x55FF55),
    SUMMER("Summer", "&e", 0xFFFF55),
    AUTUMN("Autumn", "&6", 0xFFAA55),
    WINTER("Winter", "&b", 0x55FFFF);

    private final String displayName;
    private final String colorCode;
    private final int hexColor;
    private final NamespacedKey key;

    Season(String displayName, String colorCode, int hexColor) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.hexColor = hexColor;
        this.key = new NamespacedKey("seasonalsmp", name().toLowerCase());
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public int getHexColor() {
        return hexColor;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public Season getNext() {
        return switch (this) {
            case SPRING -> SUMMER;
            case SUMMER -> AUTUMN;
            case AUTUMN -> WINTER;
            case WINTER -> SPRING;
        };
    }

    public Season getPrevious() {
        return switch (this) {
            case SPRING -> WINTER;
            case SUMMER -> SPRING;
            case AUTUMN -> SUMMER;
            case WINTER -> AUTUMN;
        };
    }

    public boolean isPeakFor(BoundType bound) {
        return bound != null && bound.getPeakSeason() == this;
    }

    public static Season fromString(String s) {
        if (s == null) return null;
        return switch (s.toUpperCase()) {
            case "SPRING" -> SPRING;
            case "SUMMER" -> SUMMER;
            case "AUTUMN", "FALL" -> AUTUMN;
            case "WINTER" -> WINTER;
            default -> null;
        };
    }
}
