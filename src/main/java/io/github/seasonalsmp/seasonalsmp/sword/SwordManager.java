package io.github.seasonalsmp.seasonalsmp.sword;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.grace.GracePeriodManager;
import io.github.seasonalsmp.seasonalsmp.seasonalblade.LegendaryItemTracker;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
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
    private final LegendaryItemTracker itemTracker;
    private final GracePeriodManager gracePeriodManager;

    public SwordManager(SeasonalSMP plugin, GracePeriodManager gracePeriodManager) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.cooldownTasks = new ConcurrentHashMap<>();
        this.cooldowns = new ConcurrentHashMap<>();
        this.boundKey = new NamespacedKey(plugin, "bound_type");
        this.itemTracker = new LegendaryItemTracker(plugin);
        this.gracePeriodManager = gracePeriodManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void initialize() {
        cooldownTasks.clear();
        cooldowns.clear();
    }

    public void shutdown() {
        for (BukkitTask task : cooldownTasks.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
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

    public void markSwordCrafted(BoundType bound) {
        if (bound == null) {
            return;
        }
        itemTracker.markCrafted(bound.name().toLowerCase() + "_sword");
    }

    public boolean canCraftSword(BoundType bound) {
        if (bound == null) {
            return false;
        }
        return itemTracker.canCraft(bound.name().toLowerCase() + "_sword");
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
        String itemKey = bound.name().toLowerCase() + "_sword";
        if (!itemTracker.canCraft(itemKey)) {
            return null;
        }
        String path = "swords." + bound.name().toLowerCase() + "-sword";
        org.bukkit.configuration.ConfigurationSection swordConfig = configManager.getConfig("swords.yml").getConfigurationSection(path);
        String materialName = swordConfig != null ? swordConfig.getString("material", "NETHERITE_SWORD") : "NETHERITE_SWORD";
        String displayName = swordConfig != null ? swordConfig.getString("display-name", bound.getColorCode() + bound.getDisplayName() + " Sword") : bound.getColorCode() + bound.getDisplayName() + " Sword";
        List<String> rawLore = swordConfig != null ? swordConfig.getStringList("lore") : new ArrayList<>();
        int modelData = swordConfig != null ? swordConfig.getInt("custom-model-data", 1000 + bound.ordinal()) : 1000 + bound.ordinal();
        boolean unbreakable = swordConfig != null ? swordConfig.getBoolean("unbreakable", true) : true;

        org.bukkit.Material material = org.bukkit.Material.getMaterial(materialName);
        if (material == null) {
            material = org.bukkit.Material.NETHERITE_SWORD;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.setDisplayName(displayName.replace("&", "§"));
        List<String> lore = new ArrayList<>();
        for (String line : rawLore) {
            lore.add(line.replace("&", "§"));
        }
        meta.setLore(lore);
        meta.setCustomModelData(modelData);
        meta.setUnbreakable(unbreakable);
        meta.getPersistentDataContainer().set(boundKey, PersistentDataType.STRING, bound.name());
        NamespacedKey abilityReadyKey = new NamespacedKey(plugin, "ability_ready");
        meta.getPersistentDataContainer().set(abilityReadyKey, PersistentDataType.BYTE, (byte) 1);
        meta.addEnchant(Enchantment.SHARPNESS, 5, true);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (gracePeriodManager.isActive()) {
            event.getInventory().setResult(new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
            return;
        }
        org.bukkit.inventory.Recipe recipe = event.getRecipe();
        if (recipe instanceof org.bukkit.inventory.ShapedRecipe shaped) {
            String key = shaped.getKey().getKey();
            BoundType bound = switch (key) {
                case "spring_sword" -> BoundType.SPRING;
                case "summer_sword" -> BoundType.SUMMER;
                case "autumn_sword" -> BoundType.AUTUMN;
                case "winter_sword" -> BoundType.WINTER;
                default -> null;
            };
            if (bound != null && !canCraftSword(bound)) {
                event.getInventory().setResult(new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
            }
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (gracePeriodManager.isActive()) {
            return;
        }
        if (!(event.getView().getPlayer() instanceof Player player)) {
            return;
        }
        org.bukkit.inventory.Recipe recipe = event.getRecipe();
        if (recipe instanceof ShapedRecipe shaped && shaped.getKey().getNamespace().equals("SeasonalSMP")) {
            String key = shaped.getKey().getKey();
            BoundType requiredBound = switch (key) {
                case "spring_sword" -> BoundType.SPRING;
                case "summer_sword" -> BoundType.SUMMER;
                case "autumn_sword" -> BoundType.AUTUMN;
                case "winter_sword" -> BoundType.WINTER;
                default -> null;
            };
            if (requiredBound == null) {
                return;
            }
            BoundType playerBound = plugin.getBoundManager().getBound(player);
            if (playerBound != requiredBound) {
                event.setCancelled(true);
                player.sendMessage("§cYou must be bound to " + requiredBound.getDisplayName() + " §cto craft this sword!");
                player.closeInventory();
                return;
            }
            if (!canCraftSword(requiredBound)) {
                event.setCancelled(true);
                event.getInventory().setResult(new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
                player.sendMessage("§cThis sword has already been forged on this server!");
                return;
            }
            markSwordCrafted(requiredBound);
            String swordName = requiredBound.getColorCode() + requiredBound.getDisplayName() + " Sword";
            Bukkit.broadcastMessage(formatOminousMessage(player.getName(), swordName));
        }
    }

    private String formatOminousMessage(String playerName, String itemName) {
        String obfuscated = "§k" + generateObfuscatedText(5) + "§r";
        return "§8" + obfuscated + " §r§8has crafted §r" + obfuscated + " §r" + itemName + " §r§8" + obfuscated + "§r";
    }

    private String generateObfuscatedText(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((char) ('\u00A0' + (int) (Math.random() * 10)));
        }
        return sb.toString();
    }
}
