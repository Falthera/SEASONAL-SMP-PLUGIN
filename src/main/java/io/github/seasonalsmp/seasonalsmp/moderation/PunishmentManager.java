package io.github.seasonalsmp.seasonalsmp.moderation;

import io.github.seasonalsmp.seasonalsmp.SeasonSMP;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class PunishmentManager {

    private final SeasonalSMP plugin;
    private final List<String> punishments;

    public PunishmentManager(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.punishments = new ArrayList<>();
        load();
    }

    public void load() {
        punishments.clear();
        List<String> configPunishments = plugin.getConfig().getStringList("moderation.punishments");
        punishments.addAll(configPunishments);
    }

    public void execute(Player target, String reason) {
        if (target == null || reason == null) {
            return;
        }
        for (String punishment : punishments) {
            executePunishment(target, punishment.trim(), reason);
        }
    }

    private void executePunishment(Player target, String punishment, String reason) {
        String lower = punishment.toLowerCase(Locale.ROOT);
        if (lower.startsWith("kick")) {
            target.kickPlayer("§cYou have been kicked.\n§7Reason: " + reason);
        } else if (lower.startsWith("ban")) {
            long duration = parseDuration(lower.replace("ban", "").trim());
            if (duration > 0) {
                target.kickPlayer("§cYou have been temporarily banned for §f" + formatDuration(duration) + "§c.\n§7Reason: " + reason);
                plugin.getServer().getBanList(org.bukkit.BanList.Type.NAME).addBan(target.getName(), "§cTemporary Ban\n§7Reason: " + reason + "\n§7Expires: " + new Date(System.currentTimeMillis() + duration), new Date(System.currentTimeMillis() + duration), null);
            } else {
                target.kickPlayer("§cYou have been permanently banned.\n§7Reason: " + reason);
                plugin.getServer().getBanList(org.bukkit.BanList.Type.NAME).addBan(target.getName(), "§cBan\n§7Reason: " + reason, null, null);
            }
        } else if (lower.startsWith("mute")) {
            long duration = parseDuration(lower.replace("mute", "").trim());
            // Mute implementation would require a mute manager; for now, we broadcast a mute message
            if (duration > 0) {
                plugin.getServer().broadcastMessage("§c" + target.getName() + " has been muted for §f" + formatDuration(duration) + "§c.\n§7Reason: " + reason);
            } else {
                plugin.getServer().broadcastMessage("§c" + target.getName() + " has been permanently muted.\n§7Reason: " + reason);
            }
        } else if (lower.equals("jail")) {
            // Teleport to spawn or a jail location
            Location spawn = plugin.getServer().getWorlds().get(0).getSpawnLocation();
            target.teleport(spawn);
            target.setGameMode(GameMode.ADVENTURE);
            target.sendMessage("§cYou have been jailed.\n§7Reason: " + reason);
        } else if (lower.startsWith("gamemode")) {
            String mode = lower.replace("gamemode", "").trim();
            try {
                GameMode gameMode = GameMode.valueOf(mode.toUpperCase(Locale.ROOT));
                target.setGameMode(gameMode);
                target.sendMessage("§cYour gamemode has been set to §f" + gameMode.name() + "§c.\n§7Reason: " + reason);
            } catch (IllegalArgumentException e) {
                // Invalid gamemode, ignore
            }
        } else if (lower.startsWith("clear-inventory") || lower.startsWith("clear")) {
            target.getInventory().clear();
            target.sendMessage("§cYour inventory has been cleared.\n§7Reason: " + reason);
        } else if (lower.startsWith("command")) {
            String cmd = punishment.substring(8).trim();
            if (!cmd.isEmpty()) {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd.replace("%player%", target.getName()));
            }
        }
    }

    private long parseDuration(String input) {
        if (input == null || input.isEmpty()) {
            return -1;
        }
        try {
            return Long.parseLong(input) * 1000L;
        } catch (NumberFormatException e) {
        }
        if (input.endsWith("d") || input.endsWith("D")) {
            try {
                return Long.parseLong(input.substring(0, input.length() - 1)) * 86400000L;
            } catch (NumberFormatException e) {
            }
        }
        if (input.endsWith("h") || input.endsWith("H")) {
            try {
                return Long.parseLong(input.substring(0, input.length() - 1)) * 3600000L;
            } catch (NumberFormatException e) {
            }
        }
        if (input.endsWith("m") || input.endsWith("M")) {
            try {
                return Long.parseLong(input.substring(0, input.length() - 1)) * 60000L;
            } catch (NumberFormatException e) {
            }
        }
        if (input.endsWith("s") || input.endsWith("S")) {
            try {
                return Long.parseLong(input.substring(0, input.length() - 1)) * 1000L;
            } catch (NumberFormatException e) {
            }
        }
        return -1;
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        if (hours > 0) {
            return hours + "h " + minutes + "m " + secs + "s";
        } else if (minutes > 0) {
            return minutes + "m " + secs + "s";
        } else {
            return secs + "s";
        }
    }

    public List<String> getPunishments() {
        return Collections.unmodifiableList(punishments);
    }
}
