package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundManager;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AbilityCommand implements CommandExecutor {

    private final SeasonalSMP plugin;
    private final BoundManager boundManager;

    public AbilityCommand(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.boundManager = plugin.getBoundManager();
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
        boundManager.activateAbility(player, bound, false);
        player.sendMessage("§aActivated " + bound.getAbilityDisplayName() + "§a!");
        return true;
    }
}
