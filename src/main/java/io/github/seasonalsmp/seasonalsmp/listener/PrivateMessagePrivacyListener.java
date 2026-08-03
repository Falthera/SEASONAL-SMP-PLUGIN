package io.github.seasonalsmp.seasonalsmp.listener;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.*;

public class PrivateMessagePrivacyListener implements Listener {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;
    private final Map<UUID, UUID> lastMessageTarget;

    public PrivateMessagePrivacyListener(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.lastMessageTarget = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (!configManager.getBoolean("privacy.hide-private-messages", true)) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        if (!isPrivateMessage(event)) {
            return;
        }

        event.setCancelled(true);
        event.setMessage("/help");
        event.setFormat(null);

        Player sender = event.getPlayer();
        String message = event.getMessage();
        String format = event.getFormat();
        Set<Player> recipients = event.getRecipients();

        for (Player recipient : recipients) {
            if (recipient != null && recipient.isOnline()) {
                String formatted = formatMessage(format, sender.getDisplayName(), message);
                plugin.getServer().getScheduler().runTask(plugin, () -> recipient.sendMessage(formatted));
            }
        }

        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[PRIVACY] Private message hidden from " + sender.getName() + " to " + getRecipientNames(recipients));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!configManager.getBoolean("privacy.hide-private-messages", true)) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        String original = event.getMessage();
        String trimmed = original.trim();
        String lower = trimmed.toLowerCase(Locale.ROOT);

        if (!isPrivateMessageCommand(lower)) {
            return;
        }

        event.setMessage("/help");
        event.setCancelled(true);

        Player sender = event.getPlayer();
        String[] parts = trimmed.split(" ", 3);

        if (parts.length < 3) {
            sender.sendMessage("§cUsage: /msg <player> <message>");
            return;
        }

        String targetName = parts[1];
        Player target = plugin.getServer().getPlayerExact(targetName);

        if (target == null || !target.isOnline()) {
            sender.sendMessage("§cPlayer not found: " + targetName);
            return;
        }

        if (target.getUniqueId().equals(sender.getUniqueId())) {
            sender.sendMessage("§cYou cannot message yourself.");
            return;
        }

        String msg = parts[2];

        sender.sendMessage("§8[§3You §8-> §3" + target.getName() + "§8]§r " + msg);
        target.sendMessage("§8[§3" + sender.getName() + " §8-> §3You§8]§r " + msg);

        lastMessageTarget.put(sender.getUniqueId(), target.getUniqueId());
        lastMessageTarget.put(target.getUniqueId(), sender.getUniqueId());
    }

    private boolean isPrivateMessage(AsyncPlayerChatEvent event) {
        Set<Player> recipients = event.getRecipients();
        if (recipients == null || recipients.size() != 2) {
            return false;
        }

        if (plugin.getServer().getOnlinePlayers().size() <= 2) {
            return false;
        }

        String format = event.getFormat();
        if (format == null) {
            return false;
        }

        String lowerFormat = format.toLowerCase(Locale.ROOT);
        return lowerFormat.contains("->") || lowerFormat.contains("whisper") || lowerFormat.contains("to ");
    }

    private boolean isPrivateMessageCommand(String lower) {
        return lower.startsWith("/msg ") || lower.startsWith("/tell ") || lower.startsWith("/w ") || lower.startsWith("/pm ") || lower.startsWith("/whisper ") || lower.startsWith("/reply ") || lower.startsWith("/r ");
    }

    private String formatMessage(String format, String playerName, String message) {
        try {
            return String.format(format, playerName, message);
        } catch (IllegalFormatException e) {
            return playerName + ": " + message;
        }
    }

    private String getRecipientNames(Set<Player> recipients) {
        List<String> names = new ArrayList<>();
        for (Player p : recipients) {
            names.add(p.getName());
        }
        return String.join(", ", names);
    }
}
