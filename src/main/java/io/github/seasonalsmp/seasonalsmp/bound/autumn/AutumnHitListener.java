package io.github.seasonalsmp.seasonalsmp.bound.autumn;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundManager;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class AutumnHitListener implements Listener {

    private final SeasonalSMP plugin;
    private final AutumnBoundHandler autumnHandler;
    private final BoundManager boundManager;

    public AutumnHitListener(SeasonalSMP plugin, AutumnBoundHandler autumnHandler) {
        this.plugin = plugin;
        this.autumnHandler = autumnHandler;
        this.boundManager = plugin.getBoundManager();
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }
        BoundType bound = boundManager.getBound(attacker);
        if (bound != BoundType.AUTUMN) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }
        if (victim.isDead() || victim.getHealth() <= 0) {
            return;
        }
        int oldStacks = autumnHandler.getStacks(victim);
        autumnHandler.onEntityHit(victim);
        int newStacks = autumnHandler.getStacks(victim);
        if (newStacks > oldStacks && newStacks > 0) {
            String stackText = "§6§lFalling Leaves §7[§e" + newStacks + "§7/§e5§7]";
            attacker.spigot().sendMessage(Component.text(stackText));
        }
    }
}
