package io.github.seasonalsmp.seasonalsmp.event.relic;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class RelicPurgeManager {

    private static RelicData data;
    private static BukkitTask broadcastTask;
    private static boolean running;

    public static void startRelicPurge(SeasonalSMP plugin) {
        if (running) {
            return;
        }
        data = new RelicData();
        data.startEvent();
        running = true;
        ConfigManager config = plugin.getConfigManager();
        broadcastTask = new BukkitRunnable() {
            int ticksLeft = config.getInt("relic-purge.duration-seconds", 600) * 20;
            @Override
            public void run() {
                if (ticksLeft <= 0) {
                    endRelicPurge(plugin);
                    return;
                }
                ticksLeft -= 20;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        Map<BoundType, List<Player>> byBound = new HashMap<>();
        for (Player p : online) {
            BoundType bound = plugin.getBoundManager().getBound(p);
            if (bound != null) {
                byBound.computeIfAbsent(bound, k -> new ArrayList<>()).add(p);
            }
        }
        Random random = new Random();
        Map<BoundType, Player> chosen = new HashMap<>();
        for (Map.Entry<BoundType, List<Player>> entry : byBound.entrySet()) {
            List<Player> list = entry.getValue();
            if (list.isEmpty()) {
                continue;
            }
            Player target = list.get(random.nextInt(list.size()));
            chosen.put(entry.getKey(), target);
            RelicType relic = RelicType.fromBound(entry.getKey());
            if (relic != null) {
                data.addRelic(target, relic);
                target.sendMessage("§6§lTHE RELIC OF " + entry.getKey().getDisplayName().toUpperCase() + " §r§6has claimed you!");
            }
        }
        Bukkit.broadcastMessage("§c§lRELIC PURGE HAS BEGUN!");
        Bukkit.broadcastMessage("§7The relics have chosen their bearers...");
    }

    public static void endRelicPurge(SeasonalSMP plugin) {
        if (!running) {
            return;
        }
        running = false;
        if (broadcastTask != null && !broadcastTask.isCancelled()) {
            broadcastTask.cancel();
        }
        if (data != null) {
            data.endEvent();
        }
        Bukkit.broadcastMessage("§c§lTHE RELIC PURGE HAS ENDED!");
    }

    public static boolean isRunning() {
        return running && data != null && data.isActive();
    }

    public static RelicData getData() {
        return data;
    }
}
