package io.github.seasonalsmp.seasonalsmp.command;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.bound.BoundManager;
import io.github.seasonalsmp.seasonalsmp.bound.BoundType;
import io.github.seasonalsmp.seasonalsmp.data.DataStorage;
import io.github.seasonalsmp.seasonalsmp.event.relic.RelicType;
import io.github.seasonalsmp.seasonalsmp.season.Season;
import io.github.seasonalsmp.seasonalsmp.gui.UIManager;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class SeasonalStartCommand implements CommandExecutor {

    private final SeasonalSMP plugin;
    private final UIManager uiManager;
    private final Random random = new Random();

    public SeasonalStartCommand(SeasonalSMP plugin) {
        this.plugin = plugin;
        this.uiManager = plugin.getUIManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("seasonalsmp.command.season.admin")) {
            sender.sendMessage("§cYou do not have permission.");
            return true;
        }

        if (args.length == 0 || !"start".equalsIgnoreCase(args[0])) {
            sender.sendMessage("§cUsage: /seasonal start");
            return true;
        }

        startSeasonalEvent(sender);
        return true;
    }

    private void startSeasonalEvent(CommandSender sender) {
        List<Player> players = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        if (players.isEmpty()) {
            sender.sendMessage("§cNo players online to assign bounds.");
            return;
        }

        BoundManager boundManager = plugin.getBoundManager();
        Map<Player, BoundType> assignments = new LinkedHashMap<>();

        for (Player player : players) {
            BoundType bound = BoundType.getRandom(random);
            assignments.put(player, bound);
            plugin.getLogger().info("Assigning bound " + bound.name() + " to " + player.getName());
        }

        Season firstSeason = Season.values()[random.nextInt(Season.values().length)];

        for (Player player : players) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 5));
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
        }

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                tick++;
                if (tick == 1) {
                    Bukkit.broadcastMessage("§0§k████§r §4§lTHE SEASONS ARE AWAKENING§r §0§k████§r");
                    Bukkit.broadcastMessage("");
                }

                if (tick >= 1 && tick <= 60) {
                    for (Player player : players) {
                        Location loc = player.getLocation().add(0, 2, 0);
                        for (int i = 0; i < 3; i++) {
                            double angle = (tick * 0.3) + (i * (Math.PI * 2 / 3));
                            double x = Math.cos(angle) * 1.5;
                            double z = Math.sin(angle) * 1.5;
                            Location particleLoc = loc.clone().add(x, 0, z);
                            player.spawnParticle(Particle.WITCH, particleLoc, 1, 0.1, 0.1, 0.1, 0.0);
                        }
                        player.spawnParticle(Particle.ENCHANT, loc, 2, 0.5, 0.5, 0.5, 0.0);
                        if (tick % 5 == 0) {
                            player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 0.3f, 0.5f + (tick * 0.02f));
                        }
                        if (tick % 10 == 0) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 20, 0));
                        }
                    }
                }

                if (tick == 60) {
                    for (Player player : players) {
                        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.8f);
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.6f);
                    }
                }

                if (tick >= 60 && tick <= 200) {
                    int index = 0;
                    for (Map.Entry<Player, BoundType> entry : assignments.entrySet()) {
                        Player player = entry.getKey();
                        BoundType bound = entry.getValue();
                        int boundTick = 60 + (index * 8);
                        if (tick == boundTick && tick <= 180) {
                            Location center = player.getLocation();
                            player.removePotionEffect(PotionEffectType.BLINDNESS);
                            player.removePotionEffect(PotionEffectType.SLOWNESS);
                            player.removePotionEffect(PotionEffectType.NAUSEA);

                            player.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 3.0f, 1.0f);
                            player.playSound(center, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2.0f, 1.0f);
                            player.playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 2.0f, 1.0f);

                            Color color = switch (bound) {
                                case SPRING -> Color.fromRGB(0x55FF55);
                                case SUMMER -> Color.fromRGB(0xFFFF55);
                                case AUTUMN -> Color.fromRGB(0xFFAA55);
                                case WINTER -> Color.fromRGB(0x55FFFF);
                            };

                            for (int i = 0; i < 8; i++) {
                                double angle = (i * Math.PI * 2) / 8;
                                double x = Math.cos(angle) * 2.5;
                                double z = Math.sin(angle) * 2.5;
                                Location fireworkLoc = center.clone().add(x, 4 + i * 0.5, z);
                                Firework firework = center.getWorld().spawn(fireworkLoc, Firework.class);
                                FireworkMeta meta = firework.getFireworkMeta();
                                FireworkEffect effect = FireworkEffect.builder()
                                        .withColor(color)
                                        .withFade(Color.WHITE)
                                        .with(FireworkEffect.Type.BURST)
                                        .withTrail()
                                        .build();
                                meta.addEffect(effect);
                                meta.setPower(1);
                                firework.setFireworkMeta(meta);
                            }

                            player.spawnParticle(Particle.FIREWORK, center, 120, 4, 5, 4, 0.15);
                            player.spawnParticle(Particle.TOTEM_OF_UNDYING, center, 40, 3, 4, 3, 0.0);
                            player.spawnParticle(Particle.HEART, center, 40, 3, 4, 3, 0.0);
                            player.spawnParticle(Particle.CRIT, center, 60, 4, 3, 4, 0.3);
                            player.spawnParticle(Particle.ENCHANT, center, 20, 2, 2, 2, 0.1);

                            boundManager.forceAssignBound(player, bound);
                            plugin.getLogger().info("Bound assigned: " + player.getName() + " -> " + bound.name());

                            String colorCode = bound.getColorCode();
                            String displayName = bound.getDisplayName();
                            player.sendTitle(colorCode + "§l" + displayName, "§r§7Your peak season is " + bound.getPeakSeason().getDisplayName() + "§7", 10, 100, 10);
                            Bukkit.broadcastMessage("§8§l[SEASONAL] §r§8" + player.getName() + " §r§8has been bound to §r" + colorCode + "§l" + displayName + "§r§8!");
                            player.sendMessage("§6§l" + displayName + " §r§6has chosen you! Your bound has been set.");

                            Vector velocity = new Vector((random.nextDouble() - 0.5) * 0.3, 0.6, (random.nextDouble() - 0.5) * 0.3);
                            player.setVelocity(velocity);
                        }
                        index++;
                    }
                }

                if (tick == 120) {
                    Bukkit.broadcastMessage("");
                    Bukkit.broadcastMessage("§0§k████████████████████████████████████");
                    Bukkit.broadcastMessage("§e§l       THE FIRST SEASON IS");
                    Bukkit.broadcastMessage("§6§l   " + firstSeason.getDisplayName().toUpperCase());
                    Bukkit.broadcastMessage("§0§k████████████████████████████████████");
                    Bukkit.broadcastMessage("");

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 5.0f, 0.5f);
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 1.0f);
                        player.playSound(player.getLocation(), Sound.MUSIC_DISC_PIGSTEP, 1.0f, 1.0f);

                        player.spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation(), 50, 3, 3, 3, 0.0);
                        player.spawnParticle(Particle.END_ROD, player.getLocation(), 100, 5, 5, 5, 0.5);
                        player.spawnParticle(Particle.ENCHANT, player.getLocation(), 30, 2, 2, 2, 0.1);
                    }

                    for (int i = 0; i < 10; i++) {
                        int finalI = i;
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    Location loc = player.getLocation().add(
                                            (random.nextDouble() - 0.5) * 20,
                                            10 + (finalI * 2),
                                            (random.nextDouble() - 0.5) * 20
                                    );
                                    player.spawnParticle(Particle.FIREWORK, loc, 30, 2, 2, 2, 0.1);
                                    player.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.0f, 1.0f);
                                }
                            }
                        }.runTaskLater(plugin, i * 5L);
                    }

                    Bukkit.broadcastMessage("");
                    Bukkit.broadcastMessage("§4§lTHE RELICS HAVE BEEN GRANTED!");
                    Bukkit.broadcastMessage("§cAll players have received the power of the relics...");

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        io.github.seasonalsmp.seasonalsmp.event.relic.RelicPurgeManager.grantRandomRelics(plugin, player);
                    }
                }

                if (tick == 200) {
                    cancel();
                    plugin.getSeasonManager().setSeason(firstSeason);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        uiManager.updateBossBar(player, firstSeason);
                    }
                    Bukkit.broadcastMessage("§a§lTHE AGE OF " + firstSeason.getDisplayName().toUpperCase() + " §r§aHAS BEGUN!");
                    plugin.getGracePeriodManager().startGracePeriod();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
