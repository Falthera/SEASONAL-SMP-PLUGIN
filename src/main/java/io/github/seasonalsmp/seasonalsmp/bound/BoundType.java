package io.github.seasonalsmp.seasonalsmp.bound;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import org.bukkit.NamespacedKey;

import java.util.*;

public enum BoundType {

    SPRING("Spring", "&a", 0x55FF55, Season.SPRING),
    SUMMER("Summer", "&e", 0xFFFF55, Season.SUMMER),
    AUTUMN("Autumn", "&6", 0xFFAA55, Season.AUTUMN),
    WINTER("Winter", "&b", 0x55FFFF, Season.WINTER);

    private final String displayName;
    private final String colorCode;
    private final int hexColor;
    private final Season peakSeason;
    private final NamespacedKey key;
    private final Map<Season, List<String>> penaltyEffects;
    private final List<String> passiveEffects;
    private final List<String> peakBonusEffects;

    BoundType(String displayName, String colorCode, int hexColor, Season peakSeason) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.hexColor = hexColor;
        this.peakSeason = peakSeason;
        this.key = new NamespacedKey("seasonalsmp", "bound_" + name().toLowerCase());
        this.penaltyEffects = new EnumMap<>(Season.class);
        this.passiveEffects = new ArrayList<>();
        this.peakBonusEffects = new ArrayList<>();
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

    public Season getPeakSeason() {
        return peakSeason;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public boolean isPeakSeason(Season season) {
        return season == peakSeason;
    }

    public List<String> getPenaltyEffects(Season season) {
        return penaltyEffects.getOrDefault(season, Collections.emptyList());
    }

    public List<String> getPassiveEffects() {
        return passiveEffects;
    }

    public List<String> getPeakBonusEffects() {
        return peakBonusEffects;
    }

    public static BoundType fromString(String s) {
        if (s == null) return null;
        return switch (s.toUpperCase()) {
            case "SPRING" -> SPRING;
            case "SUMMER" -> SUMMER;
            case "AUTUMN", "FALL" -> AUTUMN;
            case "WINTER" -> WINTER;
            default -> null;
        };
    }

    public String getAbilityDisplayName() {
        return switch (this) {
            case SPRING -> "Bloom";
            case SUMMER -> "Solar Burst";
            case AUTUMN -> "Harvest";
            case WINTER -> "Frozen Heart";
        };
    }

    public static BoundType getRandom(Random random) {
        BoundType[] values = values();
        return values[random.nextInt(values.length)];
    }

    public static BoundType fromKey(NamespacedKey key) {
        if (key == null || !"seasonalsmp".equals(key.getNamespace())) {
            return null;
        }
        String keyValue = key.getKey();
        return switch (keyValue) {
            case "bound_spring" -> SPRING;
            case "bound_summer" -> SUMMER;
            case "bound_autumn" -> AUTUMN;
            case "bound_winter" -> WINTER;
            default -> null;
        };
    }
}
