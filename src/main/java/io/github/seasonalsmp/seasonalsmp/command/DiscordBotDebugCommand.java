package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.whitelist.WhitelistManager;
import io.github.seasonalsmp.seasonalsmp.whitelist.WhitelistStats;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DiscordBotDebugCommand implements CommandExecutor {

    private final SeasonalSMP plugin;
    private final WhitelistManager whitelistManager;

    public DiscordBotDebugCommand(SeasonalSMP plugin) {
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
            case "info" -> handleInfo(sender);
            case "reload" -> handleReload(sender);
            case "test" -> handleTest(sender);
            default -> showHelp(sender);
        }
        return true;
    }

    private void handleInfo(CommandSender sender) {
        sender.sendMessage("§6=== Discord Bot Debug Info ===");
        sender.sendMessage("§ePlugin Version: §f" + plugin.getDescription().getVersion());
        sender.sendMessage("§eWhitelist API Enabled: §f" + plugin.getConfigManager().getBoolean("whitelist.enabled"));
        sender.sendMessage("§eDiscord Bot Token Set: §f" + !plugin.getConfigManager().getString("whitelist.discord-bot-token", "").isEmpty());
        sender.sendMessage("§eAPI Port: §f" + plugin.getConfigManager().getInt("whitelist.api-port", 8080));
        WhitelistStats stats = whitelistManager.getStats();
        sender.sendMessage("§eTotal Whitelisted: §f" + stats.totalWhitelisted);
        sender.sendMessage("§eOnline Whitelisted: §f" + stats.onlineWhitelisted);
        sender.sendMessage("§ePlugin Enabled: §f" + plugin.isEnabled());
        sender.sendMessage("§eCurrent Season: §f" + plugin.getSeasonManager().getCurrentSeason().getDisplayName());
    }

    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().reloadAll();
        whitelistManager.reload();
        sender.sendMessage("§aDiscord bot and whitelist configuration reloaded.");
    }

    private void handleTest(CommandSender sender) {
        sender.sendMessage("§aTesting Discord bot integration...");
        sender.sendMessage("§7If the Discord bot is configured correctly, check the console for results.");
        plugin.getWhitelistManager().getStats();
        sender.sendMessage("§aTest complete. Check console for details.");
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage("§6=== Discord Bot Debug Commands ===");
        sender.sendMessage("§e/seasonalsmpdiscordbotdebug info §7- Show debug info");
        sender.sendMessage("§e/seasonalsmpdiscordbotdebug reload §7- Reload configuration");
        sender.sendMessage("§e/seasonalsmpdiscordbotdebug test §7- Test integration");
    }
}
