package io.github.seasonalsmp.seasonalsmp.event;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.effect.particle.ParticleService;
import io.github.seasonalsmp.seasonalsmp.event.relic.RelicData;
import io.github.seasonalsmp.seasonalsmp.event.relic.RelicPurgeManager;
import io.github.seasonalsmp.seasonalsmp.event.relic.RelicType;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.Bukkit;
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
    private BukkitTask passiveTask;
    private BukkitTask passiveEffectsTask;

    public RelicPurgeListener(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.particleService = plugin.getEffectManager().getParticleService();
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
                RelicData data = RelicPurgeManager.getData();
                if (data == null) {
                    return;
                }
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (data.hasRelic(player, RelicType.BLOODBORN_RELIC)) {
                        particleService.spawn(player.getLocation(), Particle.TOTEM_OF_UNDYING, 10, 0.5);
                    } else if (data.hasRelic(player, RelicType.SPRING_RELIC)) {
                        particleService.spawn(player.getLocation(), Particle.HEART, 3, 0.5);
                    } else if (data.hasRelic(player, RelicType.SUMMER_RELIC)) {
                        particleService.spawn(player.getLocation(), Particle.FLAME, 3, 0.5);
                    } else if (data.hasRelic(player, RelicType.AUTUMN_RELIC)) {
                        particleService.spawn(player.getLocation(), Particle.CRIT, 3, 0.5);
                    } else if (data.hasRelic(player, RelicType.WINTER_RELIC)) {
                        particleService.spawn(player.getLocation(), Particle.SNOWFLAKE, 3, 0.5);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void startPassiveRelicEffects() {
        passiveEffectsTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isEnabled() || !RelicPurgeManager.isRunning()) {
                    return;
                }
                RelicData data = RelicPurgeManager.getData();
                if (data == null) {
                    return;
                }
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (!RelicPurgeManager.isRunning()) {
                        continue;
                    }
                    if (data.hasRelic(player, RelicType.SPRING_RELIC)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0));
                    }
                    if (data.hasRelic(player, RelicType.SUMMER_RELIC)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 0));
                    }
                    if (data.hasRelic(player, RelicType.AUTUMN_RELIC)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 60, 0));
                    }
                    if (data.hasRelic(player, RelicType.WINTER_RELIC)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 0));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!RelicPurgeManager.isRunning()) {
            return;
        }
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
        if (data.hasRelic(killer, RelicType.BLOODBORN_RELIC)) {
            event.setDroppedExp(event.getDroppedExp() * 4);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!RelicPurgeManager.isRunning()) {
            return;
        }
        RelicData data = RelicPurgeManager.getData();
        if (data == null) {
            return;
        }
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer != null && !killer.getUniqueId().equals(victim.getUniqueId())) {
            if (data.hasRelic(victim, RelicType.SPRING_RELIC)) {
                data.addRelic(killer, RelicType.SPRING_RELIC);
            } else if (data.hasRelic(victim, RelicType.SUMMER_RELIC)) {
                data.addRelic(killer, RelicType.SUMMER_RELIC);
            } else if (data.hasRelic(victim, RelicType.AUTUMN_RELIC)) {
                data.addRelic(killer, RelicType.AUTUMN_RELIC);
            } else if (data.hasRelic(victim, RelicType.WINTER_RELIC)) {
                data.addRelic(killer, RelicType.WINTER_RELIC);
            }
            if (data.hasAllRelics(killer)) {
                data.grantBloodbornRelic(killer);
                Bukkit.broadcastMessage("§4§l" + killer.getName() + " §r§4has collected all 4 relics and become BLOODBORN!");
            }
        }
        if (configManager.getBoolean("relic-purge.drop-on-death", true)) {
            dropRelicsOnDeath(victim, data);
        }
        data.clearPlayer(victim);
    }

    private void dropRelicsOnDeath(Player victim, RelicData data) {
        Set<RelicType> relics = data.getPlayerRelics(victim.getUniqueId());
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
        for (ItemStack item : matrix) {
            if (item != null && !item.getType().isAir()) {
                item.setAmount(item.getAmount() - 1);
            }
        }
        data.grantBloodbornRelic(player);
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
        Bukkit.broadcastMessage("§4§l" + player.getName() + " §r§4has crafted the BLOODBORN RELIC!");
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!RelicPurgeManager.isRunning()) {
            return;
        }
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
        if (data.hasRelic(damager, RelicType.SUMMER_RELIC)) {
            event.setDamage(event.getDamage() * 1.5);
        }
        if (data.hasRelic(damager, RelicType.AUTUMN_RELIC)) {
            event.setDamage(event.getDamage() * 1.25);
        }
        if (data.hasRelic(damager, RelicType.BLOODBORN_RELIC)) {
            event.setDamage(event.getDamage() * 4.0);
        }
    }
}
