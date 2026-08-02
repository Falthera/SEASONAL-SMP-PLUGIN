package io.github.seasonalsmp.seasonalsmp.seasonalblade;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.effect.particle.ParticleService;
import io.github.seasonalsmp.seasonalsmp.effect.sound.SoundService;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SeasonalBladeManager {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;
    private final ParticleService particleService;
    private final SoundService soundService;
    private final Map<UUID, BukkitTask> passiveTasks;
    private final Map<UUID, Long> cooldowns;
    private final LegendaryItemTracker itemTracker;

    public SeasonalBladeManager(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.particleService = plugin.getEffectManager().getParticleService();
        this.soundService = plugin.getEffectManager().getSoundService();
        this.passiveTasks = new ConcurrentHashMap<>();
        this.cooldowns = new ConcurrentHashMap<>();
        this.itemTracker = new LegendaryItemTracker(plugin);
    }

    public ItemStack buildSeasonalBlade() {
        if (!itemTracker.canCraft("seasonal_blade")) {
            return null;
        }
        ItemStack item = new ItemStack(Material.MACE);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.setDisplayName("&6&lSeasonal Blade");
        List<String> lore = Arrays.asList(
            "&8Legendary Weapon",
            "&7Forged from the power of every season.",
            "",
            "&aPassive: All Seasonal Passives",
            "&bActive: Season-dependent ability",
            "",
            "&eRight-click to activate your bound's power."
        );
        List<String> processedLore = new ArrayList<>();
        for (String line : lore) {
            processedLore.add(line.replace("&", "§"));
        }
        meta.setLore(processedLore);
        meta.setCustomModelData(3001);
        meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(
            new NamespacedKey(plugin, "seasonal_item_type"),
            PersistentDataType.STRING,
            "seasonal_blade"
        );
        meta.addEnchant(Enchantment.SHARPNESS, 6, true);
        meta.addEnchant(Enchantment.LOOTING, 3, true);
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        meta.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addEnchant(Enchantment.SWEEPING_EDGE, 3, true);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isSeasonalBlade(ItemStack item) {
        return SeasonalBladeType.isSeasonalBlade(item);
    }

    public void markSeasonalBladeCrafted() {
        itemTracker.markCrafted("seasonal_blade");
    }

    public boolean canCraftSeasonalBlade() {
        return itemTracker.canCraft("seasonal_blade");
    }

    public boolean isOnCooldown(Player player) {
        if (player == null) {
            return true;
        }
        Long expiry = cooldowns.get(player.getUniqueId());
        return expiry != null && System.currentTimeMillis() < expiry;
    }

    public void setCooldown(Player player, int seconds) {
        if (player == null) {
            return;
        }
        UUID uuid = player.getUniqueId();
        long expiry = System.currentTimeMillis() + (seconds * 1000L);
        cooldowns.put(uuid, expiry);
        new BukkitRunnable() {
            @Override
            public void run() {
                cooldowns.remove(uuid);
            }
        }.runTaskLater(plugin, seconds * 20L);
    }

    public void activateAbility(Player player) {
        if (player == null || !isSeasonalBlade(player.getInventory().getItemInMainHand())) {
            return;
        }
        if (isOnCooldown(player)) {
            player.sendMessage("§cAbility is on cooldown!");
            return;
        }
        Season season = plugin.getSeasonManager().getCurrentSeason();
        int cooldown = configManager.getInt("seasonal-blade.cooldown." + season.name().toLowerCase(), 60);
        String abilityName = switch (season) {
            case SPRING -> "Nature's Grasp";
            case SUMMER -> "Solar Judgment";
            case AUTUMN -> "Gale Force";
            case WINTER -> "Frostpiercer";
        };
        plugin.getUIManager().startCooldownTimer(player, abilityName, cooldown);
        setCooldown(player, cooldown);
        switch (season) {
            case SPRING -> activateSpringAbility(player);
            case SUMMER -> activateSummerAbility(player);
            case AUTUMN -> activateAutumnAbility(player);
            case WINTER -> activateWinterAbility(player);
        }
    }

    private void activateSpringAbility(Player player) {
        Location center = player.getLocation();
        double radius = 8.0;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity living && !(entity instanceof Player)) {
                Location entityLoc = entity.getLocation();
                Vector direction = center.toVector().subtract(entityLoc.toVector()).normalize();
                if (direction.length() == 0) {
                    direction = new Vector(0, 0, 1);
                }
                entity.setVelocity(direction.multiply(0.8));
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));
            }
        }
        player.getWorld().playSound(center, Sound.BLOCK_ROOTS_BREAK, 2.0f, 0.8f);
        for (int i = 0; i < 30; i++) {
            double angle = (i / 30.0) * Math.PI * 2;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            particleService.spawn(center.clone().add(x, 0, z), Particle.VILLAGER_HAPPY, 3, 0.3, 0.3, 0.3, 0.0);
        }
    }

    private void activateSummerAbility(Player player) {
        Location target = null;
        org.bukkit.block.Block targetBlock = player.getTargetBlock(null, 20);
        if (targetBlock != null) {
            target = targetBlock.getLocation();
        }
        if (target == null) {
            target = player.getLocation().add(player.getLocation().getDirection().multiply(10));
        }
        for (int y = 0; y < 20; y++) {
            particleService.spawn(target.clone().add(0, y, 0), Particle.FLAME, 2, 0.2, 0.5, 0.2, 0.0);
        }
        for (Entity entity : target.getWorld().getNearbyEntities(target, 3, 3, 3)) {
            if (entity instanceof LivingEntity living) {
                living.setFireTicks(100);
                living.damage(12.0, player);
            }
        }
        target.getWorld().playSound(target, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 3.0f, 1.0f);
        target.getWorld().playSound(target, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.8f);
    }

    private void activateAutumnAbility(Player player) {
        Vector direction = player.getLocation().getDirection().multiply(1.5);
        player.setVelocity(direction);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0));
        Location loc = player.getLocation();
        player.getWorld().playSound(loc, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 2.0f, 0.8f);
        for (int i = 0; i < 40; i++) {
            double x = (Math.random() - 0.5) * 6;
            double z = (Math.random() - 0.5) * 6;
            particleService.spawn(loc.clone().add(x, 0, z), Particle.CLOUD, 1, 0.3, 0.3, 0.3, 0.0);
        }
        for (Entity entity : player.getNearbyEntities(3, 3, 3)) {
            if (entity instanceof LivingEntity living && !(entity instanceof Player)) {
                Vector knockback = entity.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(1.2);
                entity.setVelocity(knockback);
            }
        }
    }

    private void activateWinterAbility(Player player) {
        Location target = null;
        org.bukkit.block.Block targetBlock = player.getTargetBlock(null, 15);
        if (targetBlock != null) {
            target = targetBlock.getLocation();
        }
        if (target == null) {
            target = player.getLocation().add(player.getLocation().getDirection().multiply(10));
        }
        for (int y = 0; y < 5; y++) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    Block block = target.clone().add(x, y, z).getBlock();
                    if (block.getType() == Material.AIR) {
                        particleService.spawn(block.getLocation().add(0.5, 0.5, 0.5), Particle.SNOWFLAKE, 5, 0.3, 0.3, 0.3, 0.0);
                    }
                }
            }
        }
        for (Entity entity : target.getWorld().getNearbyEntities(target, 5, 5, 5)) {
            if (entity instanceof LivingEntity living) {
                living.damage(10.0, player);
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 3));
                living.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 100, 2));
            }
        }
        target.getWorld().playSound(target, Sound.BLOCK_GLASS_BREAK, 3.0f, 0.6f);
    }

    public void startPassiveEffects(Player player) {
        if (player == null) {
            return;
        }
        UUID uuid = player.getUniqueId();
        BukkitTask existing = passiveTasks.remove(uuid);
        if (existing != null && !existing.isCancelled()) {
            existing.cancel();
        }
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    passiveTasks.remove(uuid);
                    return;
                }
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                if (!isSeasonalBlade(mainHand)) {
                    cancel();
                    passiveTasks.remove(uuid);
                    return;
                }
                applySpringPassive(player);
                applySummerPassive(player);
                applyAutumnPassive(player);
                applyWinterPassive(player);
            }
        }.runTaskTimer(plugin, 0L, 40L);
        passiveTasks.put(uuid, task);
    }

    public void stopPassiveEffects(Player player) {
        if (player == null) {
            return;
        }
        UUID uuid = player.getUniqueId();
        BukkitTask task = passiveTasks.remove(uuid);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    private void applySpringPassive(Player player) {
        if (player.getRandom().nextDouble() < 0.15) {
            player.setHealth(Math.min(player.getHealth() + 4.0, player.getMaxHealth()));
            particleService.spawn(player.getLocation().add(0, 1, 0), Particle.HEART, 3, 0.5, 0.5, 0.5, 0.0);
        }
    }

    private void applySummerPassive(Player player) {
        if (player.getRandom().nextDouble() < 0.25) {
            player.setFireTicks(60);
        }
    }

    private void applyAutumnPassive(Player player) {
        if (player.getRandom().nextDouble() < 0.3) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0));
        }
    }

    private void applyWinterPassive(Player player) {
        if (player.getRandom().nextDouble() < 0.2) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0));
        }
    }

    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (!isSeasonalBlade(weapon)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }
        Random random = new Random();
        if (random.nextDouble() < 0.2) {
            victim.setFireTicks(60);
            particleService.spawn(victim.getLocation().add(0, 1, 0), Particle.FLAME, 5, 0.3, 0.3, 0.3, 0.0);
        }
        if (random.nextDouble() < 0.15) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0));
            particleService.spawn(victim.getLocation().add(0, 1, 0), Particle.SNOWFLAKE, 5, 0.3, 0.3, 0.3, 0.0);
        }
        if (random.nextDouble() < 0.1) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 0));
        }
    }

    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (!isSeasonalBlade(mainHand)) {
            return;
        }
        if (player.getRandom().nextDouble() < 0.3) {
            particleService.spawn(player.getLocation().add(0, 0.5, 0), Particle.CLOUD, 1, 0.2, 0.2, 0.2, 0.0);
        }
    }
}
