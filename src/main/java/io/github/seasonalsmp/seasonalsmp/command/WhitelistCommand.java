package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.whitelist.WhitelistEntry;
import io.github.seasonalsmp.seasonalsmp.whitelist.WhitelistManager;
import io.github.seasonalsmp.seasonalsmp.whitelist.WhitelistStats;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class WhitelistCommand implements CommandExecutor {

    private final SeasonalSMP plugin;
    private final WhitelistManager whitelistManager;

    public WhitelistCommand(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.whitelistManager = plugin.getWhitelistManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("seasonalsmp.command.whitelist.admin")) {
            sender.sendMessage("§cYou do not have permission.");
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add" -> handleAdd(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "lookup" -> handleLookup(sender, args);
            case "list" -> handleList(sender);
            case "stats" -> handleStats(sender);
            case "reload" -> handleReload(sender);
            default -> showHelp(sender);
        }
        return true;
    }

    private void handleAdd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /whitelist add <minecraft_username>");
            return;
        }
        String username = args[1];
        sender.sendMessage("§7Submitting whitelist request for §f" + username + "§7...");
        whitelistManager.addPlayer("console", username).thenAccept(result -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (result.success) {
                    sender.sendMessage("§aSuccessfully whitelisted §f" + result.username + "§a.");
                } else {
                    sender.sendMessage("§cFailed to whitelist: §f" + result.message);
                }
            });
        });
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /whitelist remove <minecraft_username_or_uuid>");
            return;
        }
        String target = args[1];
        whitelistManager.lookupByUsername(target).thenAccept(lookup -> {
            if (lookup.found) {
                String uuid = lookup.data.get("uuid").toString();
                whitelistManager.removePlayer(uuid).thenAccept(result -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (result.success) {
                            sender.sendMessage("§aSuccessfully removed §f" + result.username + "§a from whitelist.");
                        } else {
                            sender.sendMessage("§cFailed to remove: §f" + result.message);
                        }
                    });
                });
            } else {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    sender.sendMessage("§cPlayer not found in whitelist.");
                });
            }
        });
    }

    private void handleLookup(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /whitelist lookup <minecraft_username_or_uuid>");
            return;
        }
        String target = args[1];
        whitelistManager.lookupByUsername(target).thenAccept(lookup -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (lookup.found) {
                    sender.sendMessage("§6=== Whitelist Entry ===");
                    sender.sendMessage("§eUsername: §f" + lookup.data.get("username"));
                    sender.sendMessage("§eUUID: §f" + lookup.data.get("uuid"));
                    sender.sendMessage("§eDiscord ID: §f" + lookup.data.get("discordId"));
                    sender.sendMessage("§eWhitelisted At: §f" + lookup.data.get("whitelistedAt"));
                } else {
                    whitelistManager.lookupByUuid(target).thenAccept(uuidLookup -> {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (uuidLookup.found) {
                                sender.sendMessage("§6=== Whitelist Entry ===");
                                sender.sendMessage("§eUsername: §f" + uuidLookup.data.get("username"));
                                sender.sendMessage("§eUUID: §f" + uuidLookup.data.get("uuid"));
                                sender.sendMessage("§eDiscord ID: §f" + uuidLookup.data.get("discordId"));
                                sender.sendMessage("§eWhitelisted At: §f" + uuidLookup.data.get("whitelistedAt"));
                            } else {
                                sender.sendMessage("§cPlayer not found in whitelist.");
                            }
                        });
                    });
                }
            });
        });
    }

    private void handleList(CommandSender sender) {
        List<WhitelistEntry> entries = whitelistManager.getAllEntriesSync();
        if (entries.isEmpty()) {
            sender.sendMessage("§7No players are whitelisted.");
            return;
        }
        sender.sendMessage("§6=== Whitelisted Players (" + entries.size() + ") ===");
        for (WhitelistEntry entry : entries) {
            sender.sendMessage("§f- " + entry.getUsername() + " §7(UUID: " + entry.getUuid() + ")");
        }
    }

    private void handleStats(CommandSender sender) {
        WhitelistStats stats = whitelistManager.getStats();
        sender.sendMessage("§6=== Whitelist Statistics ===");
        sender.sendMessage("§eTotal Whitelisted: §f" + stats.totalWhitelisted);
        sender.sendMessage("§eOnline Whitelisted: §f" + stats.onlineWhitelisted);
    }

    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().reloadAll();
        whitelistManager.reload();
        sender.sendMessage("§aWhitelist configuration reloaded.");
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage("§6=== Whitelist Commands ===");
        sender.sendMessage("§e/whitelist add <username> §7- Add player to whitelist");
        sender.sendMessage("§e/whitelist remove <username/uuid> §7- Remove player from whitelist");
        sender.sendMessage("§e/whitelist lookup <username/uuid> §7- Lookup whitelist entry");
        sender.sendMessage("§e/whitelist list §7- List all whitelisted players");
        sender.sendMessage("§e/whitelist stats §7- Show whitelist statistics");
        sender.sendMessage("§e/whitelist reload §7- Reload whitelist configuration");
    }
}
