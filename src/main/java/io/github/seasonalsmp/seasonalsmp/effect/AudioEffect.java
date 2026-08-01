package io.github.seasonalsmp.seasonalsmp.effect;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import io.github.seasonalsmp.seasonalsmp.effect.sound.SoundService;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

class AudioEffect implements SeasonEffectsManager.SeasonEffect {

    private final SeasonalSMP plugin;
    private final ConfigManager configManager;
    private final SoundService soundService;

    AudioEffect(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.soundService = plugin.getEffectManager().getSoundService();
    }

    @Override
    public void apply(Season season) {
    }

    @Override
    public void remove(Season season) {
    }

    @Override
    public boolean isEnabled() {
        return configManager.getBoolean("effects.ambient-sfx-enabled");
    }
}
