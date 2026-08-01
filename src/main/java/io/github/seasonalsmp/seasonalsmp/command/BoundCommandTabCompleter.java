package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class BoundCommandTabCompleter implements TabCompleter {

    private final SeasonalSMP plugin;

    public BoundCommandTabCompleter(SeasonalSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("seasonalsmp.command.bound") && !sender.isOp()) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            List<String> base = new ArrayList<>(List.of("view", "help"));
            if (sender.hasPermission("seasonalsmp.command.bound.admin")) {
                base.addAll(List.of("assign", "list"));
            }
            return base.stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        if (args.length == 2 && "assign".equalsIgnoreCase(args[0])) {
            if (sender instanceof Player player) {
                return player.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        if (args.length == 3 && "assign".equalsIgnoreCase(args[0])) {
            return Arrays.stream(new String[]{"SPRING", "SUMMER", "AUTUMN", "WINTER"})
                .filter(s -> s.startsWith(args[2].toUpperCase()))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
