package io.github.seasonalsmp.seasonalsmp.grace;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GracePeriodCommand implements CommandExecutor {

    private final SeasonalSMP plugin;
    private final GracePeriodManager gracePeriodManager;

    public GracePeriodCommand(SeasonalSMP plugin, GracePeriodManager gracePeriodManager) {
        this.plugin = plugin;
        this.gracePeriodManager = gracePeriodManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("seasonalsmp.command.grace")) {
            sender.sendMessage("§cYou do not have permission.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§cUsage: /gp <start|end|status>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> handleStart(sender);
            case "end" -> handleEnd(sender);
            case "status" -> handleStatus(sender);
            default -> sender.sendMessage("§cUsage: /gp <start|end|status>");
        }
        return true;
    }

    private void handleStart(CommandSender sender) {
        if (gracePeriodManager.isActive()) {
            sender.sendMessage("§cGrace period is already active!");
            return;
        }
        gracePeriodManager.startGracePeriod();
        sender.sendMessage("§aStarted grace period.");
    }

    private void handleEnd(CommandSender sender) {
        if (!gracePeriodManager.isActive()) {
            sender.sendMessage("§cGrace period is not active.");
            return;
        }
        gracePeriodManager.endGracePeriod();
        sender.sendMessage("§aEnded grace period.");
    }

    private void handleStatus(CommandSender sender) {
        if (gracePeriodManager.isActive()) {
            long remaining = gracePeriodManager.getRemainingSeconds();
            sender.sendMessage("§aGrace period is active. Time remaining: §f" + formatTime(remaining));
        } else {
            sender.sendMessage("§cGrace period is not active.");
        }
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
