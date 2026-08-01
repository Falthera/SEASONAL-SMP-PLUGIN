package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.trust.TrustManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TrustCommand implements CommandExecutor {

    private final SeasonalSMP plugin;
    private final TrustManager trustManager;

    public TrustCommand(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.trustManager = plugin.getTrustManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add" -> handleAdd(player, args);
            case "remove" -> handleRemove(player, args);
            case "list" -> handleList(player);
            default -> showHelp(player);
        }
        return true;
    }

    private void handleAdd(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /trust add <player>");
            return;
        }
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found or not online.");
            return;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage("§cYou cannot trust yourself.");
            return;
        }
        if (trustManager.isTrusted(player, target)) {
            player.sendMessage("§eYou already trust §f" + target.getName() + "§e.");
            return;
        }
        boolean success = trustManager.trust(player, target);
        if (success) {
            player.sendMessage("§aYou now trust §f" + target.getName() + "§a.");
            target.sendMessage("§e" + player.getName() + " §ahas added you as a trusted ally.");
        } else {
            player.sendMessage("§cFailed to add trust.");
        }
    }

    private void handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /trust remove <player>");
            return;
        }
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found or not online.");
            return;
        }
        boolean removed = trustManager.untrust(player, target);
        if (removed) {
            player.sendMessage("§aYou no longer trust §f" + target.getName() + "§a.");
            target.sendMessage("§e" + player.getName() + " §chas removed you from trusted allies.");
        } else {
            player.sendMessage("§cYou do not trust §f" + args[1] + "§c.");
        }
    }

    private void handleList(Player player) {
        List<String> names = trustManager.getTrustedNames(player);
        if (names.isEmpty()) {
            player.sendMessage("§7You have no trusted allies.");
            return;
        }
        player.sendMessage("§6=== Trusted Allies (" + names.size() + ") ===");
        for (String name : names) {
            player.sendMessage("§f- " + name);
        }
    }

    private void showHelp(Player player) {
        player.sendMessage("§6=== Trust Commands ===");
        player.sendMessage("§e/trust add <player> §7- Trust a player");
        player.sendMessage("§e/trust remove <player> §7- Remove trust from a player");
        player.sendMessage("§e/trust list §7- List your trusted allies");
    }
}
