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

    private String convertLegacyToMiniMessage(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if ((c == '&' || c == '§') && i + 1 < input.length()) {
                char next = input.charAt(i + 1);
                result.append(switch (next) {
                    case '0' -> "<black>";
                    case '1' -> "<dark_blue>";
                    case '2' -> "<dark_green>";
                    case '3' -> "<dark_aqua>";
                    case '4' -> "<dark_red>";
                    case '5' -> "<dark_purple>";
                    case '6' -> "<gold>";
                    case '7' -> "<gray>";
                    case '8' -> "<dark_gray>";
                    case '9' -> "<blue>";
                    case 'a' -> "<green>";
                    case 'b' -> "<aqua>";
                    case 'c' -> "<red>";
                    case 'd' -> "<light_purple>";
                    case 'e' -> "<yellow>";
                    case 'f' -> "<white>";
                    case 'l' -> "<bold>";
                    case 'm' -> "<strikethrough>";
                    case 'n' -> "<underline>";
                    case 'o' -> "<italic>";
                    case 'r' -> "<reset>";
                    default -> {
                        result.append(c);
                        yield "";
                    }
                });
                i++;
            } else {
                result.append(c);
            }
        }
        return result.toString();
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
        Component message = miniMessage.deserialize(convertLegacyToMiniMessage(parsed));
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
        return miniMessage.deserialize(convertLegacyToMiniMessage(raw));
    }

    public Component parse(String raw, Map<String, String> placeholders) {
        String parsed = applyPlaceholders(raw, placeholders);
        return miniMessage.deserialize(convertLegacyToMiniMessage(parsed));
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
