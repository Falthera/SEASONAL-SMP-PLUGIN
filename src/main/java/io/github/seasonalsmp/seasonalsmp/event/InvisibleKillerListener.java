package io.github.seasonalsmp.seasonalsmp.event;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public class InvisibleKillerListener implements Listener {

    private final SeasonalSMP plugin;
    private final Random random = new Random();

    public InvisibleKillerListener(SeasonalSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null || killer.equals(victim)) {
            return;
        }
        if (!killer.isInvisible()) {
            return;
        }

        Component originalDeathMessage = event.deathMessage();
        if (originalDeathMessage == null) {
            return;
        }

        String killerName = killer.getName();
        String obfuscatedKiller = generateObfuscatedText(killerName.length());
        String plainText = PlainTextComponentSerializer.plainText().serialize(originalDeathMessage);
        String modifiedText = plainText.replace(killerName, obfuscatedKiller);
        Component modifiedDeathMessage = MiniMessage.miniMessage().deserialize(modifiedText);
        event.deathMessage(modifiedDeathMessage);

        spawnInvisibleKillerVFX(killer);
    }

    private String generateObfuscatedText(int length) {
        StringBuilder sb = new StringBuilder("§k");
        for (int i = 0; i < length; i++) {
            sb.append((char) ('\u00A0' + random.nextInt(10)));
        }
        sb.append("§r");
        return sb.toString();
    }

    private void spawnInvisibleKillerVFX(Player killer) {
        if (!killer.isOnline()) {
            return;
        }

        killer.getWorld().playSound(killer.getLocation(), Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.5f);
        killer.getWorld().playSound(killer.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.8f);

        BukkitTask vfxTask = new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                if (!killer.isOnline() || tick > 100) {
                    cancel();
                    return;
                }
                if (!killer.isInvisible()) {
                    cancel();
                    return;
                }
                Location loc = killer.getLocation().clone().add(0, 1, 0);
                killer.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 30, 1.5, 2, 1.5, 0.15);
                killer.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 20, 2, 1, 2, 0.05);
                killer.getWorld().spawnParticle(Particle.DUST, loc, 15, 2, 1.5, 2, 0,
                    new Particle.DustOptions(Color.fromRGB(139, 0, 0), 3.0f));
                killer.getWorld().spawnParticle(Particle.LAVA, loc, 10, 1.5, 0.5, 1.5, 0);
                killer.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 2, 0, 0, 0, 0);
                if (tick % 20 == 0) {
                    killer.getWorld().playSound(killer.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.8f);
                    killer.getWorld().strikeLightningEffect(killer.getLocation().clone().add(
                        (random.nextDouble() - 0.5) * 6, 0, (random.nextDouble() - 0.5) * 6));
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
