package io.github.seasonalsmp.seasonalsmp.effect.sound;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.HashMap;

public class SoundService {

    private final SeasonalSMP plugin;
    private final boolean enabled;
    private final Map<String, Sound> fallbackSounds;

    public SoundService(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfigManager().getBoolean("effects.ambient-sfx-enabled");
        this.fallbackSounds = new HashMap<>();
        fallbackSounds.put("transition", Sound.ENTITY_PLAYER_LEVELUP);
        fallbackSounds.put("bloom", Sound.BLOCK_BEEHIVE_ENTER);
        fallbackSounds.put("solar_burst", Sound.ENTITY_BLAZE_SHOOT);
        fallbackSounds.put("harvest", Sound.BLOCK_CROP_BREAK);
        fallbackSounds.put("frozen_heart", Sound.BLOCK_GLASS_BREAK);
        fallbackSounds.put("sword_unsheathe", Sound.ENTITY_IRON_GOLEM_ATTACK);
        fallbackSounds.put("ui_click", Sound.UI_BUTTON_CLICK);
        fallbackSounds.put("ui_hover", Sound.UI_BUTTON_HOVER);
    }

    public void play(Player player, String key) {
        if (!enabled || player == null || !player.isOnline()) {
            return;
        }
        try {
            Sound sound = fallbackSounds.getOrDefault(key, Sound.ENTITY_PLAYER_LEVELUP);
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to play sound '" + key + "': " + e.getMessage());
        }
    }

    public void playAt(Player player, org.bukkit.Location location, String key, float volume, float pitch) {
        if (!enabled || location == null) {
            return;
        }
        try {
            Sound sound = fallbackSounds.getOrDefault(key, Sound.ENTITY_PLAYER_LEVELUP);
            location.getWorld().playSound(location, sound, volume, pitch);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to play sound at location '" + key + "': " + e.getMessage());
        }
    }
}
