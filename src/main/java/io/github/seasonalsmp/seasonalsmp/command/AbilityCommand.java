package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundManager;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.sword.SwordManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AbilityCommand implements CommandExecutor {

    private final SeasonalSMP plugin;
    private final BoundManager boundManager;
    private final SwordManager swordManager;
    private final ConfigManager configManager;

    public AbilityCommand(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.boundManager = plugin.getBoundManager();
        this.swordManager = plugin.getSwordManager();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }
        if (!player.hasPermission("seasonalsmp.command.bound")) {
            player.sendMessage("§cYou do not have permission.");
            return true;
        }
        BoundType bound = boundManager.getBound(player);
        if (bound == null) {
            player.sendMessage("§cYou do not have a bound assigned.");
            return true;
        }
        if (swordManager.isOnCooldown(player)) {
            player.sendMessage("§cAbility is on cooldown!");
            return true;
        }
        boundManager.activateAbility(player, bound, false);
        int cooldown = switch (bound) {
            case SPRING -> configManager.getInt("swords.cooldown-seconds.bloom", 60);
            case SUMMER -> configManager.getInt("swords.cooldown-seconds.solar-burst", 45);
            case AUTUMN -> configManager.getInt("swords.cooldown-seconds.harvest", 75);
            case WINTER -> configManager.getInt("swords.cooldown-seconds.frozen-heart", 40);
        };
        swordManager.setCooldown(player, cooldown);
        player.sendMessage("§aActivated " + bound.getAbilityDisplayName() + "§a!");
        return true;
    }
}
