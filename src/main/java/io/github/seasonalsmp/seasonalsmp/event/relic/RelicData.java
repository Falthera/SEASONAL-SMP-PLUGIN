package io.github.seasonalsmp.seasonalsmp.event.relic;

import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.UUID;

public class RelicData {

    private final Map<UUID, Set<RelicType>> playerRelics;
    private final Map<UUID, RelicType> activeRelics;
    private boolean active;

    public RelicData() {
        this.playerRelics = new HashMap<>();
        this.activeRelics = new HashMap<>();
        this.active = false;
    }

    public void startEvent() {
        active = true;
        playerRelics.clear();
        activeRelics.clear();
    }

    public void endEvent() {
        active = false;
        activeRelics.clear();
    }

    public boolean isActive() {
        return active;
    }

    public void addRelic(Player player, RelicType relic) {
        if (player == null || relic == null || !active) {
            return;
        }
        UUID uuid = player.getUniqueId();
        playerRelics.computeIfAbsent(uuid, k -> new HashSet<>()).add(relic);
    }

    public boolean hasRelic(Player player, RelicType relic) {
        if (player == null || relic == null) {
            return false;
        }
        return playerRelics.getOrDefault(player.getUniqueId(), Collections.emptySet()).contains(relic);
    }

    public boolean hasAllRelics(Player player) {
        if (player == null) {
            return false;
        }
        Set<RelicType> relics = playerRelics.get(player.getUniqueId());
        if (relics == null) {
            return false;
        }
        return relics.containsAll(Arrays.asList(RelicType.SPRING_RELIC, RelicType.SUMMER_RELIC, RelicType.AUTUMN_RELIC, RelicType.WINTER_RELIC));
    }

    public void grantBloodbornRelic(Player player) {
        if (player == null || !active) {
            return;
        }
        addRelic(player, RelicType.BLOODBORN_RELIC);
    }

    public boolean hasBloodbornRelic(Player player) {
        if (player == null) {
            return false;
        }
        return playerRelics.getOrDefault(player.getUniqueId(), Collections.emptySet()).contains(RelicType.BLOODBORN_RELIC);
    }

    public void clearPlayer(Player player) {
        if (player == null) {
            return;
        }
        playerRelics.remove(player.getUniqueId());
        activeRelics.remove(player.getUniqueId());
    }
}
