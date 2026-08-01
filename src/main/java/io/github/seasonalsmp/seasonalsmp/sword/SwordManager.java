package io.github.seasonalsmp.seasonalsmp.sword;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SwordManager implements Listener {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;
    private final Map<UUID, BukkitTask> cooldownTasks;
    private final Map<UUID, Long> cooldowns;
    private final NamespacedKey boundKey;

    public SwordManager(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.cooldownTasks = new ConcurrentHashMap<>();
        this.cooldowns = new ConcurrentHashMap<>();
        this.boundKey = new NamespacedKey(plugin, "bound_type");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void initialize() {
        cooldownTasks.clear();
        cooldowns.clear();
    }

    public boolean isSword(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(boundKey, PersistentDataType.STRING);
    }

    public BoundType getSwordBound(ItemStack item) {
        if (!isSword(item)) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        String value = meta.getPersistentDataContainer().get(boundKey, PersistentDataType.STRING);
        if (value == null) {
            return null;
        }
        return BoundType.fromString(value);
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
        scheduleCooldownEnd(player, seconds);
    }

    private void scheduleCooldownEnd(Player player, int seconds) {
        UUID uuid = player.getUniqueId();
        BukkitTask existing = cooldownTasks.remove(uuid);
        if (existing != null && !existing.isCancelled()) {
            existing.cancel();
        }
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                cooldowns.remove(uuid);
                cooldownTasks.remove(uuid);
                plugin.getUIManager().clearCooldown(player);
                plugin.getUIManager().showAbilityReady(player);
            }
        }.runTaskLater(plugin, seconds * 20L);
        cooldownTasks.put(uuid, task);
    }

    public void giveSword(Player player, BoundType bound) {
        if (player == null || bound == null) {
            return;
        }
        ItemStack sword = buildSword(bound);
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(sword);
        for (ItemStack drop : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), drop);
        }
    }

    public ItemStack buildSword(BoundType bound) {
        String path = "swords." + bound.name().toLowerCase() + "-sword";
        ConfigManager cm = configManager;
        String materialName = cm.getString(path + ".material", "DIAMOND_SWORD");
        String displayName = cm.getString(path + ".display-name", bound.getColorCode() + bound.getDisplayName() + " Sword");
        List<String> rawLore = cm.getStringList(path + ".lore");
        int modelData = cm.getInt(path + ".custom-model-data", 1000 + bound.ordinal());
        boolean unbreakable = cm.getBoolean(path + ".unbreakable", true);

        org.bukkit.Material material = org.bukkit.Material.getMaterial(materialName);
        if (material == null) {
            material = org.bukkit.Material.DIAMOND_SWORD;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.setDisplayName(displayName);
        List<Component> lore = new ArrayList<>();
        for (String line : rawLore) {
            lore.add(Component.text(line.replace("&", "§")));
        }
        meta.setLore(lore);
        meta.setCustomModelData(modelData);
        meta.setUnbreakable(unbreakable);
        meta.getPersistentDataContainer().set(boundKey, PersistentDataType.STRING, bound.name());
        NamespacedKey abilityReadyKey = new NamespacedKey(plugin, "ability_ready");
        meta.getPersistentDataContainer().set(abilityReadyKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        if (!isSword(player.getInventory().getItemInMainHand())) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        event.setCancelled(true);
        BoundType bound = getSwordBound(player.getInventory().getItemInMainHand());
        if (bound == null) {
            return;
        }
        if (isOnCooldown(player)) {
            return;
        }
        activateAbility(player, bound);
    }

    private void activateAbility(Player player, BoundType bound) {
        if (bound == null || player == null) {
            return;
        }
        int cooldownSeconds = switch (bound) {
            case SPRING -> configManager.getInt("swords.cooldown-seconds.bloom", 60);
            case SUMMER -> configManager.getInt("swords.cooldown-seconds.solar-burst", 45);
            case AUTUMN -> configManager.getInt("swords.cooldown-seconds.harvest", 50);
            case WINTER -> configManager.getInt("swords.cooldown-seconds.frozen-heart", 40);
        };
        plugin.getUIManager().startCooldownTimer(player, bound.getAbilityDisplayName() + " (Sword)", cooldownSeconds);
        plugin.getBoundManager().activateAbility(player, bound, true);
        setCooldown(player, cooldownSeconds);
    }
