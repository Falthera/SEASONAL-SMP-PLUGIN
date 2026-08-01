package io.github.seasonalsmp.seasonalsmp.trust;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TrustManager {

    private final SeasonalSMP plugin;
    private final File trustFile;
    private final Map<UUID, Set<UUID>> trusts;

    public TrustManager(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.trustFile = new File(plugin.getDataFolder(), "trusts.json");
        this.trusts = new ConcurrentHashMap<>();
        loadTrusts();
    }

    public void shutdown() {
        saveTrusts();
    }

    public boolean trust(Player player, Player target) {
        if (player == null || target == null) {
            return false;
        }
        if (player.getUniqueId().equals(target.getUniqueId())) {
            return false;
        }
        trusts.computeIfAbsent(player.getUniqueId(), k -> ConcurrentHashMap.newKeySet()).add(target.getUniqueId());
        saveTrusts();
        return true;
    }

    public boolean untrust(Player player, Player target) {
        if (player == null || target == null) {
            return false;
        }
        Set<UUID> set = trusts.get(player.getUniqueId());
        if (set == null) {
            return false;
        }
        boolean removed = set.remove(target.getUniqueId());
        if (set.isEmpty()) {
            trusts.remove(player.getUniqueId());
        }
        if (removed) {
            saveTrusts();
        }
        return removed;
    }

    public boolean isTrusted(Player player, Player target) {
        if (player == null || target == null) {
            return false;
        }
        Set<UUID> set = trusts.get(player.getUniqueId());
        return set != null && set.contains(target.getUniqueId());
    }

    public Set<UUID> getTrustedPlayers(Player player) {
        if (player == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(trusts.getOrDefault(player.getUniqueId(), Collections.emptySet()));
    }

    public List<String> getTrustedNames(Player player) {
        List<String> names = new ArrayList<>();
        Set<UUID> set = trusts.get(player.getUniqueId());
        if (set == null) {
            return names;
        }
        for (UUID uuid : set) {
            Player target = plugin.getServer().getPlayer(uuid);
            if (target != null) {
                names.add(target.getName());
            } else {
                names.add(uuid.toString());
            }
        }
        return names;
    }

    private void loadTrusts() {
        if (!trustFile.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(trustFile))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String json = sb.toString().trim();
            if (json.isEmpty()) {
                return;
            }
            String[] entries = json.split("\n");
            for (String entry : entries) {
                entry = entry.trim();
                if (entry.isEmpty()) {
                    continue;
                }
                String[] parts = entry.split(":", 2);
                if (parts.length == 2) {
                    UUID truster = UUID.fromString(parts[0]);
                    String[] targets = parts[1].split(",");
                    Set<UUID> set = new HashSet<>();
                    for (String target : targets) {
                        target = target.trim();
                        if (!target.isEmpty()) {
                            set.add(UUID.fromString(target));
                        }
                    }
                    if (!set.isEmpty()) {
                        trusts.put(truster, ConcurrentHashMap.newKeySet());
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load trusts: " + e.getMessage());
        }
    }

    private void saveTrusts() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(trustFile))) {
                for (Map.Entry<UUID, Set<UUID>> entry : trusts.entrySet()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(entry.getKey().toString());
                    sb.append(":");
                    boolean first = true;
                    for (UUID target : entry.getValue()) {
                        if (!first) {
                            sb.append(",");
                        }
                        sb.append(target.toString());
                        first = false;
                    }
                    writer.write(sb.toString());
                    writer.newLine();
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save trusts: " + e.getMessage());
        }
    }
}
