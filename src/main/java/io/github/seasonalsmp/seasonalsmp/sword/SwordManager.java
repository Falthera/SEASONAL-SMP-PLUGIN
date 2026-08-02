package io.github.seasonalsmp.seasonalsmp.sword;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.seasonalblade.LegendaryItemTracker;
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

    public SwordManager(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.cooldownTasks = new ConcurrentHashMap<>();
        this.cooldowns = new ConcurrentHashMap<>();
        this.boundKey = new NamespacedKey(plugin, "bound_type");
        this.itemTracker = new LegendaryItemTracker(plugin);
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
        meta.addEnchant(Enchantment.SHARPNESS, 6, true);
        meta.addEnchant(Enchantment.LOOTING, 3, true);
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        meta.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addEnchant(Enchantment.SWEEPING_EDGE, 3, true);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
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
        if (!(event.getView().getPlayer() instanceof org.bukkit.entity.Player player)) {
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
            if (bound != null) {
                if (!canCraftSword(bound)) {
                    event.setCancelled(true);
                    event.getInventory().setResult(new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
                    player.sendMessage("§cThis sword has already been forged on this server!");
                    return;
                }
                markSwordCrafted(bound);
                String swordName = bound.getColorCode() + bound.getDisplayName() + " Sword";
                Bukkit.broadcastMessage(formatOminousMessage(player.getName(), swordName));
            }
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
        String swordAbilityName = getSwordAbilityName(bound);
        int cooldownSeconds = switch (bound) {
            case SPRING -> configManager.getInt("swords.cooldown-seconds.bloom", 60);
            case SUMMER -> configManager.getInt("swords.cooldown-seconds.solar-burst", 45);
            case AUTUMN -> configManager.getInt("swords.cooldown-seconds.harvest", 50);
            case WINTER -> configManager.getInt("swords.cooldown-seconds.frozen-heart", 40);
        };
        plugin.getUIManager().startCooldownTimer(player, swordAbilityName + " (Sword)", cooldownSeconds);
        plugin.getBoundManager().activateAbility(player, bound, true);
        setCooldown(player, cooldownSeconds);
    }

    private String getSwordAbilityName(BoundType bound) {
        String path = "swords." + bound.name().toLowerCase() + "-sword.ability.name";
        org.bukkit.configuration.ConfigurationSection swordConfig = configManager.getConfig("swords.yml").getConfigurationSection("swords." + bound.name().toLowerCase() + "-sword");
        if (swordConfig != null && swordConfig.isString("ability.name")) {
            return swordConfig.getString("ability.name");
        }
        return bound.getAbilityDisplayName();
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getView().getPlayer() instanceof Player player)) {
            return;
        }
        org.bukkit.inventory.Recipe recipe = event.getRecipe();
        if (recipe instanceof ShapedRecipe shaped && shaped.getKey().getNamespace().equals("seasonalsmp")) {
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
            }
        }
    }
}
