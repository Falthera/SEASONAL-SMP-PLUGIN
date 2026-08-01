package io.github.seasonalsmp.seasonalsmp.event.relic;

import io.github.seasonalsmp.seasonalsmp.bound.BoundType;

public enum RelicType {

    SPRING_RELIC("Relic of Bloom", BoundType.SPRING),
    SUMMER_RELIC("Relic of Solstice", BoundType.SUMMER),
    AUTUMN_RELIC("Relic of Harvest", BoundType.AUTUMN),
    WINTER_RELIC("Relic of Frost", BoundType.WINTER),
    BLOODBORN_RELIC("Bloodborn Relic", null);

    private final String displayName;
    private final BoundType boundType;

    RelicType(String displayName, BoundType boundType) {
        this.displayName = displayName;
        this.boundType = boundType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BoundType getBoundType() {
        return boundType;
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
}
