package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.moderation.AdminManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommand implements CommandExecutor {

    private final SeasonalSMP plugin;
    private final AdminManager adminManager;

    public AdminCommand(SeasonalSMP plugin) {
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
            sender.sendMessage("§cUsage: /admin <player>");
            return true;
        }
        Player target = plugin.getServer().getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }
        if (adminManager.isAdmin(target)) {
            sender.sendMessage("§c" + target.getName() + " is already an admin.");
            return true;
        }
        adminManager.addAdmin(target);
        sender.sendMessage("§aAdded §f" + target.getName() + " §ato the admin list.");
        target.sendMessage("§aYou have been added to the admin list.");
        return true;
    }
}
