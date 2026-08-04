package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChangeBoundCommand implements CommandExecutor {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;

    public ChangeBoundCommand(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!configManager.getBoolean("bound.allow-change-command", false)) {
            sender.sendMessage("§cThe changebound command is currently disabled.");
            return true;
        }

        boolean isAdmin = sender.hasPermission("seasonalsmp.command.bound.admin");

        if (!isAdmin && !sender.hasPermission("seasonalsmp.command.bound")) {
            sender.sendMessage("§cYou do not have permission to change bounds.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /changebound <spring|summer|autumn|winter>");
            sender.sendMessage("§7Use /changebound <player> <bound> as an admin to change another player's bound.");
            return true;
        }

        Player target;
        BoundType bound = BoundType.fromString(args[0]);
        if (bound == null) {
            sender.sendMessage("§cInvalid bound. Available: spring, summer, autumn, winter");
            return true;
        }

        if (args.length >= 2 && isAdmin) {
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

        BoundType currentBound = plugin.getBoundManager().getBound(target);
        if (currentBound == bound) {
            sender.sendMessage("§c" + target.getName() + " is already bound to " + bound.getColorCode() + "§l" + bound.getDisplayName() + "§c!");
            return true;
        }

        if (!isAdmin && !plugin.getBoundManager().canChangeBound(target.getUniqueId())) {
            sender.sendMessage("§cYou must wait before changing your bound again!");
            return true;
        }

        plugin.getBoundManager().forceAssignBound(target, bound);

        if (!isAdmin) {
            long cooldownDays = configManager.getLong("bound.change-command-cooldown-days", 7);
            long cooldownMillis = cooldownDays * 24L * 60L * 60L * 1000L;
            plugin.getBoundManager().setBoundChangeCooldown(target.getUniqueId(), System.currentTimeMillis() + cooldownMillis);
        }

        if (sender == target) {
            sender.sendMessage("§aYour bound has been changed to " + bound.getColorCode() + "§l" + bound.getDisplayName() + "§a!");
        } else {
            sender.sendMessage("§aChanged " + target.getName() + "'s bound to " + bound.getColorCode() + "§l" + bound.getDisplayName() + "§a!");
        }
        return true;
    }
}
