package io.github.seasonalsmp.seasonalsmp.util;

import org.bukkit.entity.Player;

public final class PlayerUtil {

    private PlayerUtil() {
    }

    public static boolean isOnline(Player player) {
        return player != null && player.isOnline();
    }

    public static boolean isInWorld(Player player, String worldName) {
        return player != null && player.getWorld() != null && player.getWorld().getName().equals(worldName);
    }
}
