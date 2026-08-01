package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DebugCommand implements CommandExecutor {

    private final SeasonalSMP plugin;

    public DebugCommand(SeasonalSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("seasonalsmp.command.debug")) {
            sender.sendMessage("§cYou do not have permission.");
            return true;
        }
        Season current = plugin.getSeasonManager().getCurrentSeason();
        int boundsAssigned = plugin.getBoundManager().getAllBoundPlayers().size();
        int online = plugin.getServer().getOnlinePlayers().size();
        String worlds = String.join(", ", plugin.getServer().getWorlds().stream().map(w -> w.getName()).toList());
        sender.sendMessage("§6=== Debug Information ===");
        sender.sendMessage("§eSeason: §f" + current.getDisplayName());
        sender.sendMessage("§eNext Season: §f" + plugin.getSeasonManager().getNextSeason().getDisplayName());
        sender.sendMessage("§eNext change in: §f2 hours");
        sender.sendMessage("§eBounds Assigned: §f" + boundsAssigned);
        sender.sendMessage("§eOnline Players: §f" + online);
        sender.sendMessage("§eWorlds: §f" + worlds);
        return true;
    }
}
