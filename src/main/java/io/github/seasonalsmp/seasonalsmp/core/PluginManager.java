package io.github.seasonalsmp.seasonalsmp.core;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

public final class PluginManager {

    private final SeasonalSMP plugin;
    private boolean fullyLoaded;

    public PluginManager(SeasonalSMP plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.fullyLoaded = false;
    }

    public void markLoaded() {
        this.fullyLoaded = true;
    }

    public boolean isFullyLoaded() {
        return fullyLoaded;
    }

    public SeasonalSMP getPlugin() {
        return plugin;
    }

    public void runAsync(Runnable runnable) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    public void runSync(Runnable runnable) {
        plugin.getServer().getScheduler().runTask(plugin, runnable);
    }

    public void runLater(Runnable runnable, long delayTicks) {
        plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delayTicks);
    }

    public BukkitTask runRepeating(Runnable runnable, long delayTicks, long periodTicks) {
        return plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks);
    }

    public BukkitTask runAsyncRepeating(Runnable runnable, long delayTicks, long periodTicks) {
        return plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, delayTicks, periodTicks);
    }
}
