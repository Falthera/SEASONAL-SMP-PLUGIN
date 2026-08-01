package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundManager;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import io.github.seasonalsmp.seasonalsmp.effect.particle.ParticleService;
import io.github.seasonalsmp.seasonalsmp.effect.sound.SoundService;
import io.github.seasonalsmp.seasonalsmp.gui.UIManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class SeasonalStartCommand implements CommandExecutor {

    private final SeasonalSMP plugin;
    private final ParticleService particleService;
    private final SoundService soundService;
    private final UIManager uiManager;

    public SeasonalStartCommand(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.particleService = new ParticleService(plugin);
        this.soundService = new SoundService(plugin);
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

        Map<Player, BoundType> assignments = new LinkedHashMap<>();
        for (Player player : players) {
            BoundType bound = BoundType.getRandom(random);
            assignments.put(player, bound);
        }

        Season firstSeason = Season.values()[random.nextInt(Season.values().length)];

        Bukkit.broadcastMessage("§6§l========================================");
        Bukkit.broadcastMessage("§e§l       SEASONAL BOUND SELECTION");
        Bukkit.broadcastMessage("§6§l========================================");
        Bukkit.broadcastMessage("§7Watch the skies as your fate is decided...");
        Bukkit.broadcastMessage("");

        BukkitTask selectionTask = new BukkitRunnable() {
            int tick = 0;
            final int durationTicks = 120;

            @Override
            public void run() {
                tick++;
                if (tick > durationTicks) {
                    cancel();
                    finishSelection(assignments, firstSeason, sender);
                    return;
                }

                for (Player player : players) {
                    Location loc = player.getLocation().add(0, 2, 0);

                    if (tick % 5 == 0) {
                        particleService.spawn(loc, Particle.HEART, 10, 1.5);
                        particleService.spawn(loc, Particle.FLAME, 5, 1.0);
                    }

                    if (tick % 20 == 0) {
                        soundService.play(player, "transition");
                    }

                    if (tick % 40 == 0) {
                        player.sendTitle("§6Choosing...", "§7Your bound is being decided", 5, 20, 5);
                    }

                    if (tick == durationTicks - 20) {
                        player.sendTitle("§eAlmost there...", "§fYour destiny awaits", 5, 20, 5);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        new BukkitRunnable() {
            @Override
            public void run() {
                selectionTask.cancel();
                finishSelection(assignments, firstSeason, sender);
            }
        }.runTaskLater(plugin, durationTicks + 5L);
    }

    private void finishSelection(Map<Player, BoundType> assignments, Season firstSeason, CommandSender sender) {
        Bukkit.broadcastMessage("§6§l========================================");
        Bukkit.broadcastMessage("§e§l       BOUNDS HAVE BEEN CHOSEN");
        Bukkit.broadcastMessage("§6§l========================================");
        Bukkit.broadcastMessage("");

        for (Map.Entry<Player, BoundType> entry : assignments.entrySet()) {
            Player player = entry.getKey();
            BoundType bound = entry.getValue();

            plugin.getBoundManager().assignBound(player, bound);

            String colorCode = bound.getColorCode();
            String displayName = bound.getDisplayName();
            player.sendMessage("§aYou have been bound to " + colorCode + "§l" + displayName + "§r§a!");
            player.sendMessage("§7Your peak season is " + bound.getPeakSeason().getDisplayName() + "§7.");

            soundService.play(player, "transition");
            particleService.spawn(player.getLocation().add(0, 1, 0), Particle.TOTEM_OF_UNDYING, 50, 0.2);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                }
            }, 20L);
        }

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§6§l========================================");
        Bukkit.broadcastMessage("§e§l       THE FIRST SEASON IS " + firstSeason.getDisplayName().toUpperCase());
        Bukkit.broadcastMessage("§6§l========================================");

        plugin.getSeasonManager().setSeason(firstSeason);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }
}
