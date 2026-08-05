package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.moderation.PunishmentManager;
import io.github.seasonalsmp.seasonalsmp.moderation.WarnManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class WarnCommand implements CommandExecutor {

    private final SeasonalSMP plugin;
    private final WarnManager warnManager;
    private final PunishmentManager punishmentManager;

    public WarnCommand(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.warnManager = plugin.getWarnManager();
        this.punishmentManager = plugin.getPunishmentManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("seasonalsmp.command.warn") && !sender.isOp()) {
            sender.sendMessage("§cYou do not have permission.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /warn <player> [reason]");
            return true;
        }
        Player target = plugin.getServer().getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "No reason provided";
        warnManager.warn(target, reason);
        int count = warnManager.getWarningCount(target.getUniqueId());
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        target.sendMessage("§c§lYou have been warned! §7Reason: §f" + reason);
        target.sendMessage("§7Warning #§f" + count + " §7by §f" + sender.getName() + " §7on §f" + date);
        sender.sendMessage("§aWarned §f" + target.getName() + " §afor: §f" + reason);
        sender.sendMessage("§7Total warnings: §f" + count);
        if (count >= warnManager.getMaxWarnings()) {
            punishmentManager.execute(target, reason);
        }
        return true;
    }
}
