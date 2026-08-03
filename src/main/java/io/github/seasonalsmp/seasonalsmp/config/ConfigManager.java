package io.github.seasonalsmp.seasonalsmp.config;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public final class ConfigManager {

    private final SeasonalSMP plugin;
    private final Map<String, FileConfiguration> configurations;
    private final Map<String, File> configFiles;
    private boolean debugMode;

    private static final String[] REQUIRED_PATHS = new String[]{
        "general.plugin-prefix",
        "season.duration-seconds",
        "season.start-season",
        "season.auto-cycle",
        "world.apply-effects-to",
        "world.apply-weather-effects",
        "bound.assign-on-first-join",
        "bound.allow-change-command",
        "swords.unbreakable",
        "swords.cooldown-seconds.bloom",
        "effects.ambient-vfx-enabled",
        "ui.bossbar-enabled",
        "resource-pack.url"
    };

    public ConfigManager(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configurations = new LinkedHashMap<>();
        this.configFiles = new LinkedHashMap<>();
        this.debugMode = false;
    }

    public void loadAll() {
        registerResource("config.yml");
        registerResource("messages.yml");
        registerResource("bounds.yml");
        registerResource("swords.yml");
        registerResource("worlds.yml");
        loadWithDefaults("config.yml", "config.yml");
        loadWithDefaults("messages.yml", "messages.yml");
        loadWithDefaults("bounds.yml", "bounds.yml");
        loadWithDefaults("swords.yml", "swords.yml");
        loadWithDefaults("worlds.yml", "worlds.yml");
        reloadAll();
        validateConfigs();
    }

    private void registerResource(String name) {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File file = new File(dataFolder, name);
        configFiles.put(name, file);
    }

    private void loadWithDefaults(String source, String target) {
        File targetFile = configFiles.get(target);
        if (targetFile == null) {
            return;
        }
        if (!targetFile.exists()) {
            try {
                java.io.InputStream is = plugin.getResource(source);
                if (is == null) {
                    plugin.getLogger().warning("Missing bundled resource: " + source);
                    return;
                }
                Files.copy(is, targetFile.toPath());
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not write default config " + target, e);
            }
        }
    }

    public void reloadAll() {
        List<String> failed = new ArrayList<>();
        for (String name : configFiles.keySet()) {
            try {
                reload(name);
            } catch (Exception e) {
                failed.add(name);
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Failed to reload configuration: " + name, e);
            }
        }
        this.debugMode = getBoolean("general.debug-mode");
        if (!failed.isEmpty()) {
            plugin.getLogger().warning("Failed to reload configs: " + String.join(", ", failed));
        }
        plugin.getLogger().info("All configurations loaded. Debug mode: " + debugMode);
    }

    public void reload(String name) {
        try {
            File file = configFiles.get(name);
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            configurations.put(name, config);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to reload configuration: " + name, e);
        }
    }

    public FileConfiguration getConfig(String name) {
        return configurations.get(name);
    }

    public FileConfiguration getConfig() {
        return getConfig("config.yml");
    }

    public ConfigurationSection getSection(String path) {
        FileConfiguration config = getConfig();
        if (config == null || !config.contains(path)) {
            return null;
        }
        return config.getConfigurationSection(path);
    }

    private boolean validateConfigs() {
        List<String> missing = new ArrayList<>();
        for (String path : REQUIRED_PATHS) {
            if (!getConfig().contains(path)) {
                missing.add(path);
            }
        }
        if (!missing.isEmpty()) {
            plugin.getLogger().warning("Missing required config paths: " + String.join(", ", missing));
        }
        return missing.isEmpty();
    }

    public String getString(String path) {
        FileConfiguration config = getConfig();
        if (config == null) {
            return null;
        }
        String value = config.getString(path);
        if (value != null) {
            value = value.replaceAll("&([0-9a-fk-or])", "§$1");
        }
        return value;
    }

    public String getString(String path, String def) {
        String value = getString(path);
        return value != null ? value : (def != null ? def.replaceAll("&([0-9a-fk-or])", "§$1") : null);
    }

    public int getInt(String path) {
        return Optional.ofNullable(getConfig()).map(c -> c.getInt(path, 0)).orElse(0);
    }

    public int getInt(String path, int def) {
        return Optional.ofNullable(getConfig()).map(c -> c.getInt(path, def)).orElse(def);
    }

    public long getLong(String path) {
        return Optional.ofNullable(getConfig()).map(c -> c.getLong(path, 0L)).orElse(0L);
    }

    public long getLong(String path, long def) {
        return Optional.ofNullable(getConfig()).map(c -> c.getLong(path, def)).orElse(def);
    }

    public double getDouble(String path) {
        return Optional.ofNullable(getConfig()).map(c -> c.getDouble(path, 0.0)).orElse(0.0);
    }

    public double getDouble(String path, double def) {
        return Optional.ofNullable(getConfig()).map(c -> c.getDouble(path, def)).orElse(def);
    }

    public boolean getBoolean(String path) {
        return Optional.ofNullable(getConfig()).map(c -> c.getBoolean(path, false)).orElse(false);
    }

    public boolean getBoolean(String path, boolean def) {
        return Optional.ofNullable(getConfig()).map(c -> c.getBoolean(path, def)).orElse(def);
    }

    public List<String> getStringList(String path) {
        return Optional.ofNullable(getConfig()).map(c -> c.getStringList(path)).orElse(Collections.emptyList());
    }

    public List<Integer> getIntegerList(String path) {
        return Optional.ofNullable(getConfig()).map(c -> c.getIntegerList(path)).orElse(Collections.emptyList());
    }

    public List<Double> getDoubleList(String path) {
        return Optional.ofNullable(getConfig()).map(c -> c.getDoubleList(path)).orElse(Collections.emptyList());
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        FileConfiguration config = getConfig();
        if (config != null) {
            config.set("general.debug-mode", debugMode);
        }
    }

    public void debug(String message) {
        if (debugMode) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }

    public void reloadAllIfNeeded() {
        if (getBoolean("general.auto-reload", false)) {
            reloadAll();
        }
    }
}
