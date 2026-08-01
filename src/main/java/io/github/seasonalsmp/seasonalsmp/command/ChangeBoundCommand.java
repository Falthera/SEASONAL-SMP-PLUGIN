package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChangeBoundCommand implements CommandExecutor {

    private final SeasonalSMP plugin;

    public ChangeBoundCommand(SeasonalSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("seasonalsmp.command.bound.admin")) {
            sender.sendMessage("§cYou do not have permission.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /changebound <player> <spring|summer|autumn|winter>");
            return true;
        }
        Player target = plugin.getServer().getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }
        BoundType bound = BoundType.fromString(args[1]);
        if (bound == null) {
            sender.sendMessage("§cInvalid bound. Available: spring, summer, autumn, winter");
            return true;
        }
        plugin.getBoundManager().assignBound(target, bound);
        sender.sendMessage("§aChanged " + target.getName() + "'s bound to " + bound.getDisplayName() + "§a.");
        return true;
    }
}
