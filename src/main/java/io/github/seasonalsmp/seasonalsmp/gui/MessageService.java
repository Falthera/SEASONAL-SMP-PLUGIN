package io.github.seasonalsmp.seasonalsmp.gui;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;

public class MessageService {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;
    private final MiniMessage miniMessage;

    public MessageService(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.miniMessage = MiniMessage.miniMessage();
    }

    public void send(Player player, String key) {
        send(player, key, Collections.emptyMap());
    }

    public void send(Player player, String key, Map<String, String> placeholders) {
        if (player == null) {
            return;
        }
        String raw = getRaw(key);
        if (raw == null) {
            return;
        }
        String parsed = applyPlaceholders(raw, placeholders);
        Component message = miniMessage.deserialize(parsed);
        player.sendMessage(message);
    }

    public String applyPlaceholders(String raw, Map<String, String> placeholders) {
        String result = raw;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    public Component parse(String raw) {
        return miniMessage.deserialize(raw);
    }

    public Component parse(String raw, Map<String, String> placeholders) {
        String parsed = applyPlaceholders(raw, placeholders);
        return miniMessage.deserialize(parsed);
    }

    public String parseRaw(String raw) {
        return miniMessage.serialize(parse(raw));
    }

    private String getRaw(String key) {
        org.bukkit.configuration.ConfigurationSection messages = configManager.getConfig("messages.yml");
        if (messages == null) {
            return null;
        }
        return messages.getString(key);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
