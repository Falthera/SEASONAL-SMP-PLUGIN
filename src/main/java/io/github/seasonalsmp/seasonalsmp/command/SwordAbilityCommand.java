package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundManager;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.seasonalblade.SeasonalBladeManager;
import io.github.seasonalsmp.seasonalsmp.sword.SwordManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SwordAbilityCommand implements CommandExecutor {

    private final SeasonalSMP plugin;
    private final BoundManager boundManager;
    private final SwordManager swordManager;
    private final SeasonalBladeManager seasonalBladeManager;

    public SwordAbilityCommand(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.boundManager = plugin.getBoundManager();
        this.swordManager = plugin.getSwordManager();
        this.seasonalBladeManager = plugin.getSeasonalBladeManager();
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

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand == null || mainHand.getType().isAir() || !mainHand.hasItemMeta()) {
            player.sendMessage("§cYou must be holding a bound sword or the Seasonal Blade to use this command.");
            return true;
        }

        if (seasonalBladeManager.isSeasonalBlade(mainHand)) {
            if (seasonalBladeManager.isOnCooldown(player)) {
                player.sendMessage("§cSeasonal Blade ability is on cooldown!");
                return true;
            }
            seasonalBladeManager.activateAbility(player);
            return true;
        }

        if (swordManager.isSword(mainHand)) {
            BoundType bound = swordManager.getSwordBound(mainHand);
            if (bound == null) {
                player.sendMessage("§cThis sword is not bound to any element.");
                return true;
            }
            BoundType playerBound = boundManager.getBound(player);
            if (playerBound != bound) {
                player.sendMessage("§cYou must be bound to " + bound.getColorCode() + "§l" + bound.getDisplayName() + " §cto use this sword's ability!");
                return true;
            }
            if (swordManager.isOnCooldown(player)) {
                player.sendMessage("§cSword ability is on cooldown!");
                return true;
            }
            boundManager.activateAbility(player, bound, true);
            return true;
        }

        player.sendMessage("§cYou must be holding a bound sword or the Seasonal Blade to use this command.");
        return true;
    }
}
