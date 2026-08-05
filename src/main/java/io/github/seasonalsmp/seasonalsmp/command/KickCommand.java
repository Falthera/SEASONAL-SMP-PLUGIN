package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KickCommand implements CommandExecutor {

    private final SeasonalSMP plugin;

    public KickCommand(SeasonalSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("seasonalsmp.command.kick")) {
            sender.sendMessage("§cYou do not have permission.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /kick <player> [reason]");
            return true;
        }
        Player target = plugin.getServer().getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }
        String reason = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "No reason provided";
        target.kickPlayer("§cYou have been kicked.\n§7Reason: " + reason);
        plugin.getServer().broadcastMessage("§c" + target.getName() + " was kicked by §f" + sender.getName() + "§c.\n§7Reason: " + reason);
        return true;
    }
}
