package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundManager;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import io.github.seasonalsmp.seasonalsmp.gui.UIManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class SeasonalStartCommand implements CommandExecutor {

    private final SeasonalSMP plugin;
    private final UIManager uiManager;

    public SeasonalStartCommand(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.uiManager = plugin.getUIManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("seasonalsmp.command.season.admin")) {
            sender.sendMessage("§cYou do not have permission.");
            return true;
        }

        if (args.length == 0 || !"start".equalsIgnoreCase(args[0])) {
            sender.sendMessage("§cUsage: /seasonal start");
            return true;
        }

        startSeasonalEvent(sender);
        return true;
    }

    private void startSeasonalEvent(CommandSender sender) {
        List<Player> players = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        if (players.isEmpty()) {
            sender.sendMessage("§cNo players online to assign bounds.");
            return;
        }

        BoundManager boundManager = plugin.getBoundManager();
        Random random = new Random();

        for (Player player : players) {
            BoundType bound = BoundType.getRandom(random);
            boundManager.forceAssignBound(player, bound);
        }

        Season firstSeason = Season.values()[random.nextInt(Season.values().length)];
        plugin.getSeasonManager().setSeason(firstSeason);

        Bukkit.broadcastMessage("§6§l========================================");
        Bukkit.broadcastMessage("§e§l       SEASONAL BOUND SELECTION");
        Bukkit.broadcastMessage("§6§l========================================");
        Bukkit.broadcastMessage("§aAll players have been bound and the season has started!");
        Bukkit.broadcastMessage("§7Current Season: §f" + firstSeason.getDisplayName());
        Bukkit.broadcastMessage("§6§l========================================");

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }
}
