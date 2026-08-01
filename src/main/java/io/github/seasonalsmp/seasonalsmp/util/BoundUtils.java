package io.github.seasonalsmp.seasonalsmp.bound;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import io.github.seasonalsmp.seasonalsmp.sword.SwordManager;
import org.bukkit.entity.Player;

import java.util.Random;

public class BoundUtils {

    private final SeasonalSMP plugin;
    private final SwordManager swordManager;
    private final Random random;

    public BoundUtils(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.swordManager = plugin.getSwordManager();
        this.random = new Random();
    }

    public BoundType assignRandom(Player player) {
        return BoundType.getRandom(random);
    }

    public void applyPassiveEffects(Player player, Season currentSeason) {
        if (player == null || !player.isOnline()) {
            return;
        }
        BoundType bound = plugin.getBoundManager().getBound(player);
        if (bound == null) {
            return;
        }
    }

    public void applyPenaltyEffects(Player player, BoundType bound, Season currentSeason) {
        if (player == null || !player.isOnline() || bound == null || currentSeason == null) {
            return;
        }
    }

    public void applyPeakEffects(Player player, BoundType bound) {
        if (player == null || !player.isOnline() || bound == null) {
            return;
        }
    }
}
