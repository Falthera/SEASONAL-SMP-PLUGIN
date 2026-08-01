package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveSwordCommand implements CommandExecutor {

    private final SeasonalSMP plugin;

    public GiveSwordCommand(SeasonalSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("seasonalsmp.command.give")) {
            sender.sendMessage("§cYou do not have permission.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /givesword <spring|summer|autumn|winter> [player]");
            return true;
        }
        BoundType bound = BoundType.fromString(args[0]);
        if (bound == null) {
            sender.sendMessage("§cInvalid bound. Available: spring, summer, autumn, winter");
            return true;
        }
        Player target;
        if (args.length >= 2) {
            target = plugin.getServer().getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found.");
                return true;
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage("§cSpecify a player.");
            return true;
        }
        plugin.getSwordManager().giveSword(target, bound);
        if (sender == target) {
            sender.sendMessage("§aReceived " + bound.getColorCode() + "§l" + bound.getDisplayName() + " Sword§a!");
        } else {
            sender.sendMessage("§aGave " + bound.getColorCode() + "§l" + bound.getDisplayName() + " Sword§a to " + target.getName() + "!");
        }
        return true;
    }
}
