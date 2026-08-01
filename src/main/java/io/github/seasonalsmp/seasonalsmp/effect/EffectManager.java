package io.github.seasonalsmp.seasonalsmp.effect;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.effect.particle.ParticleService;
import io.github.seasonalsmp.seasonalsmp.effect.sound.SoundService;

import java.util.Objects;

public class EffectManager {

    private final SeasonalSMP plugin;
    private final ParticleService particleService;
    private final SoundService soundService;

    public EffectManager(SeasonalSMP plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.particleService = new ParticleService(plugin);
        this.soundService = new SoundService(plugin);
    }

    public void initialize() {
    }

    public void shutdown() {
    }

    public ParticleService getParticleService() {
        return particleService;
    }

    public SoundService getSoundService() {
        return soundService;
    }
}
