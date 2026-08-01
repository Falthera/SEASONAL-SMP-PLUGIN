package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;

import java.util.HashMap;
import java.util.Map;

public class ReloadCommand implements org.bukkit.command.CommandExecutor {

    private final SeasonalSMP plugin;

    public ReloadCommand(SeasonalSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!sender.hasPermission("seasonalsmp.command.reload")) {
            sender.sendMessage("§cYou do not have permission.");
            return true;
        }
        try {
            plugin.getConfigManager().reloadAll();
            sender.sendMessage("§aConfiguration reloaded successfully.");
            return true;
        } catch (Exception e) {
            sender.sendMessage("§cFailed to reload configuration! Check console.");
            return true;
        }
    }
}
