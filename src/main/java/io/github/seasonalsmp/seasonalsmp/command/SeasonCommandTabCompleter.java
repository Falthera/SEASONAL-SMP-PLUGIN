package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SeasonCommandTabCompleter implements TabCompleter {

    private final SeasonalSMP plugin;

    public SeasonCommandTabCompleter(SeasonalSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("seasonalsmp.command.season") && !sender.isOp()) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            List<String> base = new ArrayList<>(List.of("info", "time", "help"));
            if (sender.hasPermission("seasonalsmp.command.season.admin")) {
                base.addAll(List.of("set", "next"));
            }
            return base.stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        if (args.length == 2 && "set".equalsIgnoreCase(args[0])) {
            return Arrays.stream(Season.values())
                .map(Season::name)
                .filter(s -> s.startsWith(args[1].toUpperCase()))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
