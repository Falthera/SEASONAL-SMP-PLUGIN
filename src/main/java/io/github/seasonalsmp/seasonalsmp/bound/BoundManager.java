package io.github.seasonalsmp.seasonalsmp.bound;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.autumn.AutumnBoundHandler;
import io.github.seasonalsmp.seasonalsmp.bound.spring.SpringBoundHandler;
import io.github.seasonalsmp.seasonalsmp.bound.summer.SummerBoundHandler;
import io.github.seasonalsmp.seasonalsmp.bound.winter.WinterBoundHandler;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.data.BoundDataService;
import io.github.seasonalsmp.seasonalsmp.effect.sound.SoundService;
import io.github.seasonalsmp.seasonalsmp.gui.MessageService;
import io.github.seasonalsmp.seasonalsmp.gui.UIManager;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import io.github.seasonalsmp.seasonalsmp.sword.SwordManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class BoundManager {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;
    private final BoundDataService boundDataService;
    private final MessageService messageService;
    private final UIManager uiManager;
    private final SwordManager swordManager;
    private final SoundService soundService;
    private final SpringBoundHandler springHandler;
    private final SummerBoundHandler summerHandler;
    private final AutumnBoundHandler autumnHandler;
    private final WinterBoundHandler winterHandler;
    private final java.util.Random random;
    private org.bukkit.scheduler.BukkitTask passiveTask;

    public BoundManager(SeasonalSMP plugin, io.github.seasonalsmp.seasonalsmp.data.DataStorage dataStorage) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.boundDataService = new BoundDataService(plugin, dataStorage);
        this.boundDataService.initialize();
        this.messageService = new MessageService(plugin);
        this.uiManager = plugin.getUIManager();
        this.swordManager = plugin.getSwordManager();
        this.soundService = new SoundService(plugin);
        this.springHandler = new SpringBoundHandler(plugin);
        this.summerHandler = new SummerBoundHandler(plugin);
        this.autumnHandler = new AutumnBoundHandler(plugin);
        this.winterHandler = new WinterBoundHandler(plugin);
        this.random = new java.util.Random();
        startPassiveEffects();
    }

    private void startPassiveEffects() {
        passiveTask = new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isEnabled()) {
                    return;
                }
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    applyActiveEffects(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 100L);
    }

    public void shutdown() {
        if (passiveTask != null && !passiveTask.isCancelled()) {
            passiveTask.cancel();
        }
        saveAll();
    }

    public void applyActiveEffects(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        BoundType bound = getBound(player);
        if (bound == null) {
            return;
        }
        Season current = plugin.getSeasonManager().getCurrentSeason();
        switch (bound) {
            case SPRING -> springHandler.applyPassiveEffects(player, current);
            case SUMMER -> summerHandler.applyPassiveEffects(player, current);
            case AUTUMN -> autumnHandler.applyPassiveEffects(player, current);
            case WINTER -> winterHandler.applyPassiveEffects(player, current);
        }
    }

    public BoundType getBound(Player player) {
        if (player == null) {
            return null;
        }
        return boundDataService.getBound(player);
    }

    public boolean hasBound(Player player) {
        return getBound(player) != null;
    }

    public boolean assignBound(Player player, BoundType bound) {
        if (player == null || bound == null) {
            return false;
        }
        if (hasBound(player)) {
            return false;
        }
        boundDataService.setBound(player, bound);
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            messageService.send(player, "bound.assign.first-join",
                Map.of("bound", bound.getColorCode(), "bound_name", bound.getDisplayName()));
            soundService.play(player, "transition");
            if (configManager.getBoolean("swords.give-on-bound-assign")) {
                swordManager.giveSword(player, bound);
            }
            uiManager.updateBossBar(player, plugin.getSeasonManager().getCurrentSeason());
        });
        return true;
    }

    public void forceAssignBound(Player player, BoundType bound) {
        if (player == null || bound == null) {
            return;
        }
        boundDataService.setBound(player, bound);
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            messageService.send(player, "bound.assign.first-join",
                Map.of("bound", bound.getColorCode(), "bound_name", bound.getDisplayName()));
            soundService.play(player, "transition");
            if (configManager.getBoolean("swords.give-on-bound-assign")) {
                swordManager.giveSword(player, bound);
            }
            uiManager.updateBossBar(player, plugin.getSeasonManager().getCurrentSeason());
        });
    }

    public boolean assignRandomBound(Player player) {
        if (player == null) {
            return false;
        }
        BoundType randomBound;
        if (configManager.getBoolean("general.first-join-bound-random")) {
            randomBound = BoundType.getRandom(random);
        } else {
            String defaultBound = configManager.getString("general.default-bound", "NONE");
            randomBound = BoundType.fromString(defaultBound);
            if (randomBound == null) {
                randomBound = BoundType.SPRING;
            }
        }
        return assignBound(player, randomBound);
    }

    public Set<UUID> getAllBoundPlayers() {
        return boundDataService.getAllBoundPlayers();
    }

    public void saveAll() {
        boundDataService.saveAll();
    }

    public void loadAll() {
        boundDataService.loadAll();
    }

    public void activateAbility(Player player, BoundType bound, boolean swordAbility) {
        if (player == null || bound == null) {
            return;
        }
        switch (bound) {
            case SPRING -> {
                if (swordAbility) {
                    springHandler.activateSwordAbility(player);
                } else {
                    springHandler.activateBoundAbility(player);
                }
            }
            case SUMMER -> {
                if (swordAbility) {
                    summerHandler.activateSwordAbility(player);
                } else {
                    summerHandler.activateBoundAbility(player);
                }
            }
            case AUTUMN -> {
                if (swordAbility) {
                    autumnHandler.activateSwordAbility(player);
                } else {
                    autumnHandler.activateBoundAbility(player);
                }
            }
            case WINTER -> {
                if (swordAbility) {
                    winterHandler.activateSwordAbility(player);
                } else {
                    winterHandler.activateBoundAbility(player);
                }
            }
        }
    }
}
