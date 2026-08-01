package io.github.seasonalsmp.seasonalsmp.core;

import io.github.seasonalsmp.seasonalsmp.season.SeasonManager;
import org.bukkit.scheduler.BukkitRunnable;

public class SeasonCycleTask extends BukkitRunnable {

    private final SeasonManager manager;

    public SeasonCycleTask(SeasonManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        manager.advanceSeason();
    }
}
