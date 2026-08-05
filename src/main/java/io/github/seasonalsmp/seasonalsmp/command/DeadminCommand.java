package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.moderation.AdminManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeadminCommand implements CommandExecutor {

    private final SeasonalSMP plugin;
    private final AdminManager adminManager;

    public DeadminCommand(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.adminManager = plugin.getAdminManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§cYou must be an operator to use this command.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /deadmin <player>");
            return true;
        }
        Player target = plugin.getServer().getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }
        if (!adminManager.isAdmin(target)) {
            sender.sendMessage("§c" + target.getName() + " is not in the admin list.");
            return true;
        }
        adminManager.removeAdmin(target);
        sender.sendMessage("§aRemoved §f" + target.getName() + " §afrom the admin list.");
        target.sendMessage("§cYou have been removed from the admin list.");
        return true;
    }
}
