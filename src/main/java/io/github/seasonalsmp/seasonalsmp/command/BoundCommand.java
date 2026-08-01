package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BoundCommand implements CommandExecutor {

    private final SeasonalSMP plugin;

    public BoundCommand(SeasonalSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showOwnBound(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "view" -> viewBound(sender, args);
            case "assign" -> assignBound(sender, args);
            case "list" -> listBounds(sender);
            case "help" -> showHelp(sender);
            default -> sender.sendMessage("§cUsage: /bound [view|assign <player> <season>|list|help]");
        }
        return true;
    }

    private void showOwnBound(CommandSender sender) {
        if (!sender.hasPermission("seasonalsmp.command.bound")) {
            sender.sendMessage("§cYou do not have permission.");
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can view their own bound.");
            return;
        }
        BoundType bound = plugin.getBoundManager().getBound(player);
        if (bound == null) {
            sender.sendMessage("§7You do not have a bound assigned.");
            return;
        }
        sender.sendMessage("§fYour Season Bound: " + bound.getColorCode() + "§l" + bound.getDisplayName() + "§r");
    }

    private void viewBound(CommandSender sender, String[] args) {
        if (!sender.hasPermission("seasonalsmp.command.bound")) {
            sender.sendMessage("§cYou do not have permission.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /bound view <player>");
            return;
        }
        Player target = plugin.getServer().getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return;
        }
        BoundType bound = plugin.getBoundManager().getBound(target);
        if (bound == null) {
            sender.sendMessage("§7" + target.getName() + " has no bound assigned.");
            return;
        }
        sender.sendMessage("§f" + target.getName() + "'s Bound: " + bound.getColorCode() + "§l" + bound.getDisplayName() + "§r");
    }

    private void assignBound(CommandSender sender, String[] args) {
        if (!sender.hasPermission("seasonalsmp.command.bound.admin")) {
            sender.sendMessage("§cYou do not have permission.");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /bound assign <player> <season>");
            return;
        }
        Player target = plugin.getServer().getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return;
        }
        BoundType bound = BoundType.fromString(args[2]);
        if (bound == null) {
            sender.sendMessage("§cInvalid season. Available: Spring, Summer, Autumn, Winter");
            return;
        }
        boolean result = plugin.getBoundManager().assignBound(target, bound);
        if (!result) {
            sender.sendMessage("§cPlayer already has a bound.");
            return;
        }
        sender.sendMessage("§aAssigned " + bound.getColorCode() + "§l" + bound.getDisplayName() + "§r§a Bound to " + target.getName() + ".");
    }

    private void listBounds(CommandSender sender) {
        if (!sender.hasPermission("seasonalsmp.command.bound.admin")) {
            sender.sendMessage("§cYou do not have permission.");
            return;
        }
        sender.sendMessage("§6=== Bound Players ===");
        int count = 0;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            BoundType bound = plugin.getBoundManager().getBound(player);
            if (bound != null) {
                sender.sendMessage(" §f- " + player.getName() + ": " + bound.getColorCode() + bound.getDisplayName());
                count++;
            }
        }
        sender.sendMessage("§7Total: §f" + count);
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage("§6=== Bound Commands ===");
        sender.sendMessage("§e/bound view [player] §7- View bound");
        sender.sendMessage("§e/bound assign <player> <season> §7- Assign bound");
        sender.sendMessage("§e/bound list §7- List all assigned bounds");
        sender.sendMessage("§e/bound help §7- Show this help");
    }
}
