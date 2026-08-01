package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SeasonCommand implements CommandExecutor {

    private final SeasonalSMP plugin;

    public SeasonCommand(SeasonalSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showSeasonInfo(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "info" -> showSeasonInfo(sender);
            case "set" -> setSeason(sender, args);
            case "next" -> advanceSeason(sender);
            case "time" -> showCycleTime(sender);
            case "help" -> showHelp(sender);
            default -> sender.sendMessage("§cUsage: /season [info|set <season>|next|time|help]");
        }
        return true;
    }

    private void showSeasonInfo(CommandSender sender) {
        Season current = plugin.getSeasonManager().getCurrentSeason();
        sender.sendMessage("§6=== Season Information ===");
        sender.sendMessage("§eCurrent Season: §f" + current.getDisplayName());
        sender.sendMessage("§eNext change: §f2 hours");
        if (sender instanceof Player player && plugin.getBoundManager().hasBound(player)) {
            BoundType bound = plugin.getBoundManager().getBound(player);
            sender.sendMessage("§eYour Bound: §f" + bound.getDisplayName());
        }
    }

    private void setSeason(CommandSender sender, String[] args) {
        if (!sender.hasPermission("seasonalsmp.command.season.admin")) {
            sender.sendMessage("§cYou do not have permission.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /season set <season>");
            return;
        }
        Season season = Season.fromString(args[1]);
        if (season == null) {
            sender.sendMessage("§cInvalid season. Available: Spring, Summer, Autumn, Winter");
            return;
        }
        plugin.getSeasonManager().setSeason(season);
        sender.sendMessage("§aSeason set to " + season.getDisplayName() + ".");
    }

    private void advanceSeason(CommandSender sender) {
        if (!sender.hasPermission("seasonalsmp.command.season.admin")) {
            sender.sendMessage("§cYou do not have permission.");
            return;
        }
        plugin.getSeasonManager().advanceSeason();
        sender.sendMessage("§aSeason advanced.");
    }

    private void showCycleTime(CommandSender sender) {
        if (!sender.hasPermission("seasonalsmp.command.season")) {
            sender.sendMessage("§cYou do not have permission.");
            return;
        }
        Season current = plugin.getSeasonManager().getCurrentSeason();
        sender.sendMessage("§eCurrent Season: §f" + current.getDisplayName());
        sender.sendMessage("§eNext Season: §f" + plugin.getSeasonManager().getNextSeason().getDisplayName());
        sender.sendMessage("§eNext change in: §f2 hours");
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage("§6=== Seasonal SMP Commands ===");
        sender.sendMessage("§e/season info §7- Show current season info");
        sender.sendMessage("§e/season set <season> §7- Set current season");
        sender.sendMessage("§e/season next §7- Advance to next season");
        sender.sendMessage("§e/season time §7- Show time remaining");
        sender.sendMessage("§e/season help §7- Show this help");
    }
}
