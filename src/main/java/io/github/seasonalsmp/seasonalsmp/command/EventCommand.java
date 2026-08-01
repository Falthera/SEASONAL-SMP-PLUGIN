package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public class EventCommand implements CommandExecutor, TabCompleter {

    private final SeasonalSMP plugin;

    public EventCommand(SeasonalSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("seasonalsmp.command.season.admin")) {
            sender.sendMessage("§cYou do not have permission.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /event start <event-name>");
            return true;
        }
        if (!"start".equalsIgnoreCase(args[0])) {
            sender.sendMessage("§cUsage: /event start <event-name>");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /event start <event-name>");
            return true;
        }
        String eventName = args[1].toUpperCase();
        if (!"RELIC-PURGE".equalsIgnoreCase(eventName)) {
            sender.sendMessage("§cUnknown event. Available: RELIC-PURGE");
            return true;
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            io.github.seasonalsmp.seasonalsmp.event.RelicPurgeManager.startRelicPurge(plugin);
        });
        sender.sendMessage("§aStarted RELIC-PURGE event!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("seasonalsmp.command.season.admin")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return List.of("start");
        }
        if (args.length == 2 && "start".equalsIgnoreCase(args[0])) {
            return List.of("RELIC-PURGE");
        }
        return Collections.emptyList();
    }
}
