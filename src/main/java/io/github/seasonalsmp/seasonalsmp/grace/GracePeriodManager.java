package io.github.seasonalsmp.seasonalsmp.grace;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ConcurrentHashMap;

public class GracePeriodManager {

    private final SeasonalSMP plugin;
    private BukkitTask timerTask;
    private volatile long endTime;
    private volatile boolean active;

    public GracePeriodManager(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.active = false;
        this.endTime = 0L;
        long savedEndTime = plugin.getDataStorage().getGraceEndTime();
        if (savedEndTime > System.currentTimeMillis()) {
            this.endTime = savedEndTime;
            this.active = true;
            long remainingSeconds = getRemainingSeconds();
            startTimer(remainingSeconds);
            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.getUIManager().showGraceBossBar(player, remainingSeconds, remainingSeconds);
            }
        }
    }

    public void startGracePeriod() {
        if (isActive()) {
            return;
        }
        long durationSeconds = plugin.getConfigManager().getLong("grace-period.duration-seconds", 3600);
        this.endTime = System.currentTimeMillis() + (durationSeconds * 1000L);
        this.active = true;
        plugin.getDataStorage().setGraceEndTime(endTime);
        plugin.getDataStorage().persistGrace();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle("§a§lGRACE PERIOD", "§7Peace has been restored...", 10, 80, 20);
            player.sendMessage("§a§lGRACE PERIOD §r§7has begun! No damage will be dealt for §f" + formatTime(durationSeconds) + "§7.");
            plugin.getUIManager().showGraceBossBar(player, durationSeconds, durationSeconds);
        }
        Bukkit.broadcastMessage("§a§lGRACE PERIOD §r§7has begun! Sword crafting has been disabled and all damage is blocked for §f" + formatTime(durationSeconds) + "§7.");

        startTimer(durationSeconds);
    }

    public void shutdown() {
        if (timerTask != null && !timerTask.isCancelled()) {
            timerTask.cancel();
        }
        timerTask = null;
        this.active = false;
        this.endTime = 0L;
        plugin.getDataStorage().setGraceEndTime(0L);
        plugin.getDataStorage().persistGrace();
    }

    public void endGracePeriod() {
        if (!isActive()) {
            return;
        }
        this.active = false;
        this.endTime = 0L;
        plugin.getDataStorage().setGraceEndTime(0L);
        plugin.getDataStorage().persistGrace();
        if (timerTask != null && !timerTask.isCancelled()) {
            timerTask.cancel();
        }
        timerTask = null;

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle("§c§lGRACE PERIOD ENDED", "§7The peace has shattered...", 10, 80, 20);
            plugin.getUIManager().hideGraceBossBar(player);
        }
        Bukkit.broadcastMessage("§c§lGRACE PERIOD §r§7has ended! Sword crafting is re-enabled and damage is now active.");
    }

    public boolean isActive() {
        if (!active) {
            return false;
        }
        if (System.currentTimeMillis() >= endTime) {
            endGracePeriod();
            return false;
        }
        return true;
    }

    public long getRemainingSeconds() {
        if (!isActive()) {
            return 0;
        }
        return Math.max(0, (endTime - System.currentTimeMillis()) / 1000L);
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
        this.active = endTime > System.currentTimeMillis();
        if (active && timerTask == null) {
            long remainingSeconds = getRemainingSeconds();
            startTimer(remainingSeconds);
        }
    }

    private void startTimer(long durationSeconds) {
        if (timerTask != null && !timerTask.isCancelled()) {
            timerTask.cancel();
        }
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }
                long remaining = getRemainingSeconds();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    plugin.getUIManager().updateGraceBossBarProgress(player, durationSeconds, remaining);
                }
                if (remaining <= 0) {
                    endGracePeriod();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
