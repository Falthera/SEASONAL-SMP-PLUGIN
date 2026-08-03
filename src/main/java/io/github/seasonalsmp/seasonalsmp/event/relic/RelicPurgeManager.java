package io.github.seasonalsmp.seasonalsmp.event.relic;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.data.DataStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
        DataStorage storage = plugin.getDataStorage();
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        for (Player player : online) {
            UUID uuid = player.getUniqueId();
            storage.addRelic(uuid, RelicType.SPRING_RELIC);
            storage.addRelic(uuid, RelicType.SUMMER_RELIC);
            storage.addRelic(uuid, RelicType.AUTUMN_RELIC);
            storage.addRelic(uuid, RelicType.WINTER_RELIC);
            if (storage.hasAllRelics(uuid)) {
                storage.grantBloodborn(uuid);
            }
            for (RelicType relic : RelicType.values()) {
                if (relic == RelicType.BLOODBORN_RELIC) {
                    continue;
                }
                ItemStack relicItem = relic.createItem();
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(relicItem);
                if (!leftover.isEmpty()) {
                    player.sendMessage("§c§lYour inventory is full! Drop something to make room for the relic!");
                    for (ItemStack drop : leftover.values()) {
                        Item entity = player.getWorld().dropItemNaturally(player.getLocation(), drop);
                        entity.setPickupDelay(0);
                        entity.setTicksLived(1);
                    }
                }
            }
            if (storage.hasBloodborn(uuid)) {
                player.sendMessage("§4§lTHE BLOODBORN RELIC HAS CLAIMED YOU!");
                ItemStack bloodborn = RelicType.BLOODBORN_RELIC.createItem();
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(bloodborn);
                if (!leftover.isEmpty()) {
                    player.sendMessage("§c§lYour inventory is full! Drop something to make room for the Bloodborn Relic!");
                    for (ItemStack drop : leftover.values()) {
                        Item entity = player.getWorld().dropItemNaturally(player.getLocation(), drop);
                        entity.setPickupDelay(0);
                        entity.setTicksLived(1);
                    }
                }
            } else {
                player.sendMessage("§6§lALL RELICS HAVE BEEN CLAIMED!");
            }
        }
        Bukkit.broadcastMessage("§c§lRELIC PURGE HAS BEGUN!");
        Bukkit.broadcastMessage("§7All relics have been granted to every player...");
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
