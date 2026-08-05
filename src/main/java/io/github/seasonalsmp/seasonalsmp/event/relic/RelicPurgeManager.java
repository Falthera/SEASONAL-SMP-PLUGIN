package io.github.seasonalsmp.seasonalsmp.event.relic;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.data.DataStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class RelicPurgeManager {

    private static RelicData data;
    private static BukkitTask broadcastTask;
    private static BukkitTask vfxTask;
    private static boolean running;

    public static void startRelicPurge(SeasonalSMP plugin) {
        if (running) {
            return;
        }
        data = new RelicData();
        data.startEvent();
        running = true;
        ConfigManager config = plugin.getConfigManager();
        int durationSeconds = config.getInt("relic-purge.duration-seconds", 3600);
        for (Player player : new ArrayList<>(Bukkit.getOnlinePlayers())) {
            plugin.getUIManager().showPurgeBossBar(player, durationSeconds, durationSeconds);
        }
        broadcastTask = new BukkitRunnable() {
            int ticksLeft = durationSeconds * 20;
            @Override
            public void run() {
                if (ticksLeft <= 0) {
                    endRelicPurge(plugin);
                    return;
                }
                int remainingSeconds = ticksLeft / 20;
                for (Player player : new ArrayList<>(Bukkit.getOnlinePlayers())) {
                    plugin.getUIManager().updatePurgeBossBarProgress(player, durationSeconds, remainingSeconds);
                }
                ticksLeft -= 20;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        for (Player player : online) {
            player.sendTitle("§4§lRELIC PURGE", "§7The relics have awakened...", 10, 80, 20);
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.8f);
        }
        Bukkit.broadcastMessage("§4§lTHE RELIC PURGE HAS BEGUN!");
        Bukkit.broadcastMessage("§cThe sky bleeds... the relics have been unleashed...");

        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                if (!running) {
                    cancel();
                    return;
                }
                for (Player player : online) {
                    if (!player.isOnline()) continue;
                    for (double y = player.getLocation().getY() + 40; y > player.getLocation().getY() - 5; y -= 2) {
                        player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(0, y - player.getLocation().getY(), 0), 3, 8, 0.2, 8, 0,
                            new Particle.DustOptions(org.bukkit.Color.fromRGB(139, 0, 0), 2.0f));
                    }
                    if (tick % 5 == 0) {
                        player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, player.getLocation().clone().add(0, 2, 0), 20, 3, 2, 3, 0.05);
                    }
                    if (tick % 40 == 0) {
                        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.8f);
                        player.getWorld().strikeLightningEffect(player.getLocation().clone().add(
                            (Math.random() - 0.5) * 20, 0, (Math.random() - 0.5) * 20));
                    }
                }
                tick++;
            }
        }.runTaskTimer(plugin, 20L, 2L);

        for (Player player : online) {
            grantRandomRelics(plugin, player);
        }
    }

    private static void grantRelicsAndBloodborn(SeasonalSMP plugin, Player player) {
        DataStorage storage = plugin.getDataStorage();
        UUID uuid = player.getUniqueId();
        storage.addRelic(uuid, RelicType.SPRING_RELIC);
        storage.addRelic(uuid, RelicType.SUMMER_RELIC);
        storage.addRelic(uuid, RelicType.AUTUMN_RELIC);
        storage.addRelic(uuid, RelicType.WINTER_RELIC);
        if (storage.hasAllRelics(uuid)) {
            storage.grantBloodborn(uuid);
        }
        List<RelicType> allRelics = new ArrayList<>();
        for (RelicType relic : RelicType.values()) {
            if (relic == RelicType.BLOODBORN_RELIC) continue;
            allRelics.add(relic);
        }
        for (RelicType relic : allRelics) {
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
            player.sendTitle("§4§lBLOODBORN", "§7You have been claimed...", 10, 100, 20);
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 2.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 1.0f, 0.8f);
            new BukkitRunnable() {
                int t = 0;
                @Override
                public void run() {
                    if (!player.isOnline() || t > 40) {
                        cancel();
                        return;
                    }
                    Location loc = player.getLocation().clone().add(0, 1, 0);
                    player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 30, 1, 2, 1, 0.1);
                    player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 20, 2, 1, 2, 0.05);
                    player.getWorld().spawnParticle(Particle.DUST, loc, 10, 1, 1, 1, 0,
                        new Particle.DustOptions(org.bukkit.Color.fromRGB(139, 0, 0), 2.5f));
                    t++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
            ItemStack bloodborn = RelicType.BLOODBORN_RELIC.createItem();
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(bloodborn);
            if (!leftover.isEmpty()) {
                for (ItemStack drop : leftover.values()) {
                    Item entity = player.getWorld().dropItemNaturally(player.getLocation(), drop);
                    entity.setPickupDelay(0);
                    entity.setTicksLived(1);
                }
            }
        } else {
            player.sendMessage("§6§lALL RELICS HAVE BEEN CLAIMED!");
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.8f);
        }
    }

    private static void grantRandomRelics(SeasonalSMP plugin, Player player) {
        DataStorage storage = plugin.getDataStorage();
        UUID uuid = player.getUniqueId();
        List<RelicType> baseRelics = new ArrayList<>(List.of(
            RelicType.SPRING_RELIC, RelicType.SUMMER_RELIC,
            RelicType.AUTUMN_RELIC, RelicType.WINTER_RELIC
        ));
        Collections.shuffle(baseRelics);
        int count = 1 + new java.util.Random().nextInt(2);
        for (int i = 0; i < Math.min(count, baseRelics.size()); i++) {
            storage.addRelic(uuid, baseRelics.get(i));
            ItemStack relicItem = baseRelics.get(i).createItem();
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(relicItem);
            if (!leftover.isEmpty()) {
                for (ItemStack drop : leftover.values()) {
                    Item entity = player.getWorld().dropItemNaturally(player.getLocation(), drop);
                    entity.setPickupDelay(0);
                    entity.setTicksLived(1);
                }
            }
        }
        if (storage.hasAllRelics(uuid)) {
            storage.grantBloodborn(uuid);
            player.sendTitle("§4§lBLOODBORN", "§7You have been claimed...", 10, 100, 20);
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 2.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 1.0f, 0.8f);
            new BukkitRunnable() {
                int t = 0;
                @Override
                public void run() {
                    if (!player.isOnline() || t > 40) {
                        cancel();
                        return;
                    }
                    Location loc = player.getLocation().clone().add(0, 1, 0);
                    player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 30, 1, 2, 1, 0.1);
                    player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 20, 2, 1, 2, 0.05);
                    player.getWorld().spawnParticle(Particle.DUST, loc, 10, 1, 1, 1, 0,
                        new Particle.DustOptions(org.bukkit.Color.fromRGB(139, 0, 0), 2.5f));
                    t++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
            ItemStack bloodborn = RelicType.BLOODBORN_RELIC.createItem();
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(bloodborn);
            if (!leftover.isEmpty()) {
                for (ItemStack drop : leftover.values()) {
                    Item entity = player.getWorld().dropItemNaturally(player.getLocation(), drop);
                    entity.setPickupDelay(0);
                    entity.setTicksLived(1);
                }
            }
        } else {
            player.sendMessage("§6§lA relic has been claimed!");
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.8f);
        }
    }

    public static void grantRelicsToPlayer(SeasonalSMP plugin, Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        DataStorage storage = plugin.getDataStorage();
        UUID uuid = player.getUniqueId();
        storage.addRelic(uuid, RelicType.SPRING_RELIC);
        storage.addRelic(uuid, RelicType.SUMMER_RELIC);
        storage.addRelic(uuid, RelicType.AUTUMN_RELIC);
        storage.addRelic(uuid, RelicType.WINTER_RELIC);
        if (storage.hasAllRelics(uuid)) {
            storage.grantBloodborn(uuid);
        }
        for (RelicType relic : RelicType.values()) {
            if (relic == RelicType.BLOODBORN_RELIC) continue;
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
            player.sendTitle("§4§lBLOODBORN", "§7You have been claimed...", 10, 100, 20);
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 2.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 1.0f, 0.8f);
            new BukkitRunnable() {
                int t = 0;
                @Override
                public void run() {
                    if (!player.isOnline() || t > 40) {
                        cancel();
                        return;
                    }
                    Location loc = player.getLocation().clone().add(0, 1, 0);
                    player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 30, 1, 2, 1, 0.1);
                    player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 20, 2, 1, 2, 0.05);
                    player.getWorld().spawnParticle(Particle.DUST, loc, 10, 1, 1, 1, 0,
                        new Particle.DustOptions(org.bukkit.Color.fromRGB(139, 0, 0), 2.5f));
                    t++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
            ItemStack bloodborn = RelicType.BLOODBORN_RELIC.createItem();
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(bloodborn);
            if (!leftover.isEmpty()) {
                for (ItemStack drop : leftover.values()) {
                    Item entity = player.getWorld().dropItemNaturally(player.getLocation(), drop);
                    entity.setPickupDelay(0);
                    entity.setTicksLived(1);
                }
            }
        } else {
            player.sendMessage("§6§lALL RELICS HAVE BEEN CLAIMED!");
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.8f);
        }
    }

    public static void endRelicPurge(SeasonalSMP plugin) {
        if (!running) {
            return;
        }
        running = false;
        if (broadcastTask != null && !broadcastTask.isCancelled()) {
            broadcastTask.cancel();
        }
        if (vfxTask != null && !vfxTask.isCancelled()) {
            vfxTask.cancel();
        }
        if (data != null) {
            data.endEvent();
        }
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        for (Player player : online) {
            plugin.getUIManager().hidePurgeBossBar(player);
            player.sendTitle("§8§lTHE END", "§7The relics fade into darkness...", 10, 80, 20);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1.5f, 0.8f);
        }
        Bukkit.broadcastMessage("§8§lTHE RELIC PURGE HAS ENDED!");
        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                if (t > 60) {
                    cancel();
                    return;
                }
                for (Player player : online) {
                    if (!player.isOnline()) continue;
                    Location loc = player.getLocation().clone().add(0, 2, 0);
                    player.getWorld().spawnParticle(Particle.EXPLOSION, loc, 5, 4, 2, 4, 0);
                    player.getWorld().spawnParticle(Particle.LARGE_SMOKE, loc, 10, 3, 1, 3, 0.02);
                    player.getWorld().spawnParticle(Particle.FLAME, loc, 15, 2, 1, 2, 0.01);
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public static boolean isRunning() {
        return running && data != null && data.isActive();
    }

    public static RelicData getData() {
        return data;
    }

    public static void shutdown() {
        endRelicPurge(io.github.seasonalsmp.seasonalsmp.SeasonalSMP.get());
    }
}
