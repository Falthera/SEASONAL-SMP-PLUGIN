package io.github.seasonalsmp.seasonalsmp.event;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.data.DataStorage;
import io.github.seasonalsmp.seasonalsmp.effect.particle.ParticleService;
import io.github.seasonalsmp.seasonalsmp.event.relic.RelicData;
import io.github.seasonalsmp.seasonalsmp.event.relic.RelicPurgeManager;
import io.github.seasonalsmp.seasonalsmp.event.relic.RelicType;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class RelicPurgeListener implements Listener {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;
    private final ParticleService particleService;
    private final DataStorage dataStorage;
    private BukkitTask passiveTask;
    private BukkitTask passiveEffectsTask;

    public RelicPurgeListener(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.particleService = plugin.getEffectManager().getParticleService();
        this.dataStorage = plugin.getDataStorage();
    }

    public void initialize() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startPassiveEffects();
        startPassiveRelicEffects();
    }

    private void startPassiveEffects() {
        passiveTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isEnabled() || !RelicPurgeManager.isRunning()) {
                    return;
                }
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    if (dataStorage.hasBloodborn(uuid)) {
                        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().clone().add(0, 1, 0), 25, 1.5, 1.5, 1.5, 0.15);
                        player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, player.getLocation().clone().add(0, 1, 0), 15, 2, 1, 2, 0.05);
                        player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation().clone().add(0, 1.5, 0), 12, 1.5, 1, 1.5, 0,
                            new Particle.DustOptions(Color.fromRGB(139, 0, 0), 2.5f));
                        if (player.isOnGround()) {
                            player.getWorld().spawnParticle(Particle.LAVA, player.getLocation().clone().add(0, 0.1, 0), 8, 1.5, 0.1, 1.5, 0);
                        }
                    } else if (dataStorage.hasRelic(uuid, RelicType.SPRING_RELIC)) {
                        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().clone().add(0, 1.2, 0), 5, 0.8, 0.6, 0.8, 0.02);
                        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().clone().add(0, 0.5, 0), 3, 0.6, 0.4, 0.6, 0.01);
                    } else if (dataStorage.hasRelic(uuid, RelicType.SUMMER_RELIC)) {
                        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().clone().add(0, 1, 0), 8, 0.8, 0.8, 0.8, 0.04);
                        player.getWorld().spawnParticle(Particle.LAVA, player.getLocation().clone().add(0, 0.2, 0), 4, 0.6, 0.1, 0.6, 0);
                    } else if (dataStorage.hasRelic(uuid, RelicType.AUTUMN_RELIC)) {
                        player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().clone().add(0, 1, 0), 6, 0.8, 0.6, 0.8, 0.03);
                        player.getWorld().spawnParticle(Particle.SOUL, player.getLocation().clone().add(0, 0.5, 0), 3, 0.6, 0.4, 0.6, 0.01);
                    } else if (dataStorage.hasRelic(uuid, RelicType.WINTER_RELIC)) {
                        player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().clone().add(0, 1.2, 0), 8, 0.8, 0.6, 0.8, 0.02);
                        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().clone().add(0, 0.3, 0), 3, 0.6, 0.2, 0.6, 0.01);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }

    private void startPassiveRelicEffects() {
        passiveEffectsTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isEnabled() || !RelicPurgeManager.isRunning()) {
                    return;
                }
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    if (dataStorage.hasRelic(uuid, RelicType.SPRING_RELIC)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0));
                    }
                    if (dataStorage.hasRelic(uuid, RelicType.SUMMER_RELIC)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 0));
                    }
                    if (dataStorage.hasRelic(uuid, RelicType.AUTUMN_RELIC)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 60, 0));
                    }
                    if (dataStorage.hasRelic(uuid, RelicType.WINTER_RELIC)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 0));
                    }
                    if (dataStorage.hasBloodborn(uuid)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 1));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        RelicData data = RelicPurgeManager.getData();
        if (data == null) {
            return;
        }
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) {
            return;
        }
        if (killer.getUniqueId().equals(entity.getUniqueId())) {
            return;
        }
        if (dataStorage.hasBloodborn(killer.getUniqueId())) {
            event.setDroppedExp(event.getDroppedExp() * 4);
            killer.getWorld().spawnParticle(PotionEffectType.HEALTH_BOOST.getType().getKey().getKey().equals("health_boost")
                ? Particle.TOTEM_OF_UNDYING : Particle.TOTEM_OF_UNDYING, entity.getLocation().clone().add(0, 1, 0), 40, 1.5, 1, 1.5, 0.1);
            killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (configManager.getBoolean("relic-purge.drop-on-death", true)) {
            dropRelicsOnDeath(event.getEntity());
        }
        Player victim = event.getEntity();
        if (dataStorage.hasBloodborn(victim.getUniqueId())) {
            victim.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, victim.getLocation().clone().add(0, 1, 0), 60, 2, 2, 2, 0.2);
            victim.getWorld().spawnParticle(Particle.LARGE_SMOKE, victim.getLocation().clone().add(0, 1, 0), 30, 2, 1, 2, 0.03);
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.5f, 0.8f);
        }
    }

    private void dropRelicsOnDeath(Player victim) {
        Set<RelicType> relics = dataStorage.getPlayerRelics(victim.getUniqueId());
        if (relics == null || relics.isEmpty()) {
            return;
        }
        for (RelicType relic : relics) {
            ItemStack relicItem = relic.createItem();
            Item entity = victim.getWorld().dropItemNaturally(victim.getLocation(), relicItem);
            entity.setPickupDelay(0);
            entity.setTicksLived(1);
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!RelicPurgeManager.isRunning()) {
            return;
        }
        RelicData data = RelicPurgeManager.getData();
        if (data == null) {
            return;
        }
        CraftingInventory inventory = event.getInventory();
        ItemStack[] matrix = inventory.getMatrix();
        Set<RelicType> foundRelics = new HashSet<>();
        for (ItemStack item : matrix) {
            if (item == null || item.getType().isAir()) {
                continue;
            }
            RelicType relic = RelicType.fromItem(item);
            if (relic == null || relic == RelicType.BLOODBORN_RELIC) {
                return;
            }
            foundRelics.add(relic);
        }
        if (foundRelics.size() != 4) {
            return;
        }
        event.setCancelled(true);
        Player player = (Player) event.getView().getPlayer();
        UUID uuid = player.getUniqueId();
        for (ItemStack item : matrix) {
            if (item != null && !item.getType().isAir()) {
                item.setAmount(item.getAmount() - 1);
            }
        }
        dataStorage.addRelic(uuid, RelicType.BLOODBORN_RELIC);
        dataStorage.grantBloodborn(uuid);
        player.sendTitle("§4§lBLOODBORN", "§7You have ascended...", 10, 100, 20);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0f, 1.5f);
        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                if (!player.isOnline() || t > 50) {
                    cancel();
                    return;
                }
                Location loc = player.getLocation().clone().add(0, 1.5, 0);
                player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 40, 1.5, 2, 1.5, 0.15);
                player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 25, 2.5, 1, 2.5, 0.05);
                player.getWorld().spawnParticle(Particle.REDSTONE, loc, 15, 2, 1.5, 2, 0,
                    new Particle.DustOptions(Color.fromRGB(139, 0, 0), 3.0f));
                player.getWorld().spawnParticle(Particle.LAVA, loc, 10, 1.5, 0.5, 1.5, 0);
                if (t % 10 == 0) {
                    player.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
                }
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
        Bukkit.broadcastMessage("§4§l" + player.getName() + " §r§4has crafted the BLOODBORN RELIC!");
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        RelicData data = RelicPurgeManager.getData();
        if (data == null) {
            return;
        }
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        Player damager = null;
        if (event.getDamager() instanceof Player p) {
            damager = p;
        } else if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player p) {
            damager = p;
        } else if (event.getDamager() instanceof Tameable pet && pet.getOwner() instanceof Player p) {
            damager = p;
        }
        if (damager == null || damager.getUniqueId().equals(victim.getUniqueId())) {
            return;
        }
        UUID damagerUuid = damager.getUniqueId();
        double bonus = 1.0;
        if (dataStorage.hasRelic(damagerUuid, RelicType.SUMMER_RELIC)) {
            bonus += 0.5;
        }
        if (dataStorage.hasRelic(damagerUuid, RelicType.AUTUMN_RELIC)) {
            bonus += 0.25;
        }
        if (dataStorage.hasRelic(damagerUuid, RelicType.BLOODBORN_RELIC)) {
            bonus += 3.0;
        }
        if (bonus > 1.0) {
            event.setDamage(event.getDamage() * bonus);
            victim.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, victim.getLocation().clone().add(0, 1, 0), 8, 0.5, 0.5, 0.5, 0.1);
        }
        if (dataStorage.hasBloodborn(damagerUuid)) {
            damager.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, damager.getLocation().clone().add(0, 1.5, 0), 5, 0.5, 0.5, 0.5, 0.05);
        }
    }
}
