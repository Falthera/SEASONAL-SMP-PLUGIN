package io.github.seasonalsmp.seasonalsmp.gui;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UIManager {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;
    private final Map<UUID, BossBar> playerBossBars;
    private final Map<UUID, BukkitTask> cooldownTasks;
    private boolean initialized;

    public UIManager(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.playerBossBars = new ConcurrentHashMap<>();
        this.cooldownTasks = new ConcurrentHashMap<>();
        this.initialized = false;
    }

    public void initialize() {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                updateBossBar(player, plugin.getSeasonManager().getCurrentSeason());
            }
        });
        this.initialized = true;
    }

    public void shutdown() {
        for (BukkitTask task : cooldownTasks.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        cooldownTasks.clear();
        for (BossBar bar : playerBossBars.values()) {
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                online.hideBossBar(bar);
            }
        }
        playerBossBars.clear();
    }

    public void updateBossBar(Player player, Season season) {
        if (player == null || !configManager.getBoolean("ui.bossbar-enabled")) {
            return;
        }
        UUID uuid = player.getUniqueId();
        BossBar oldBar = playerBossBars.remove(uuid);
        if (oldBar != null) {
            player.hideBossBar(oldBar);
        }
        String prefix = configManager.getString("general.plugin-prefix", "&r");
        Component name = Component.text(prefix).append(Component.space())
            .append(Component.text(season.getDisplayName(), TextColor.color(season.getHexColor())));
        float progress = 1.0f;
        BossBar.Color barColor = switch (season) {
            case SPRING -> BossBar.Color.GREEN;
            case SUMMER -> BossBar.Color.YELLOW;
            case AUTUMN -> BossBar.Color.YELLOW;
            case WINTER -> BossBar.Color.BLUE;
        };
        BossBar.Overlay overlay = BossBar.Overlay.PROGRESS;
        BossBar bar = BossBar.bossBar(name, progress, barColor, overlay);
        player.showBossBar(bar);
        playerBossBars.put(uuid, bar);
    }

    public void removeBossBar(Player player) {
        if (player == null) {
            return;
        }
        UUID uuid = player.getUniqueId();
        BossBar bar = playerBossBars.remove(uuid);
        if (bar != null) {
            player.hideBossBar(bar);
        }
    }

    public void showSeasonChangeTitle(Player player, Season newSeason) {
        if (player == null || !configManager.getBoolean("ui.title-on-transition")) {
            return;
        }
        String titleRaw = configManager.getString("season.transition.title", "<gold>Season Changed!</gold>");
        String subtitleRaw = configManager.getString("season.transition.subtitle", "<white>The season is now {season_name}</white>");
        subtitleRaw = subtitleRaw.replace("{season_name}", newSeason.getDisplayName()).replace("{season}", newSeason.getColorCode());
        String seasonTag = newSeason.getColorCode();
        String processedTitle = titleRaw.replace("{season}", seasonTag);
        Component title = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(processedTitle);
        Component subtitle = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(subtitleRaw);
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            player.showTitle(net.kyori.adventure.title.Title.title(title, subtitle));
        });
    }

    public void showActionBar(Player player, Component message) {
        if (player == null || !configManager.getBoolean("ui.actionbar-enabled")) {
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> player.sendActionBar(message));
    }

    public void showCooldown(Player player, String abilityName, int secondsRemaining) {
        if (player == null || !configManager.getBoolean("ui.cooldown-display")) {
            return;
        }
        String raw = configManager.getString("season.info.cooldown", "<white>Cooldown: <red>{seconds}s</red></white>");
        String formatted = raw.replace("{seconds}", String.valueOf(secondsRemaining));
        showActionBar(player, MiniMessage.miniMessage().deserialize(formatted));
    }

    public void clearCooldown(Player player) {
        if (player == null) {
            return;
        }
        UUID uuid = player.getUniqueId();
        BukkitTask task = cooldownTasks.remove(uuid);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        showActionBar(player, Component.text(" "));
    }

    public void showAbilityReady(Player player) {
        if (player == null || !configManager.getBoolean("ui.cooldown-display")) {
            return;
        }
        String raw = configManager.getString("season.info.ready", "<green><b>Ability Ready!</b></green>");
        showActionBar(player, MiniMessage.miniMessage().deserialize(raw));
    }

    public void startCooldownTimer(Player player, String abilityName, int totalSeconds) {
        if (player == null || !configManager.getBoolean("ui.cooldown-display")) {
            return;
        }
        UUID uuid = player.getUniqueId();
        BukkitTask existing = cooldownTasks.remove(uuid);
        if (existing != null && !existing.isCancelled()) {
            existing.cancel();
        }
        BukkitTask task = new BukkitRunnable() {
            int remaining = totalSeconds;
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                if (remaining <= 0) {
                    cancel();
                    cooldownTasks.remove(uuid);
                    showAbilityReady(player);
                    return;
                }
                showCooldown(player, abilityName, remaining);
                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        cooldownTasks.put(uuid, task);
    }

    public boolean isInitialized() {
        return initialized;
    }
}
