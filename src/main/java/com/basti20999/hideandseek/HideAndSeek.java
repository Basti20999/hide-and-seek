package com.basti20999.hideandseek;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HideAndSeek extends JavaPlugin implements Listener {
    private List<Player> seekers = new ArrayList<>();
    private List<Player> hiders = new ArrayList<>();
    private Location spawnPoint;
    private boolean gameStarted = false;
    private boolean hidingPhase = false;
    private long hidingTimeEnd;
    private static final long HIDING_TIME_SECONDS = 60; // 10 minutes

    @Override
    public void onEnable() {
        // Load configuration
        saveDefaultConfig();
        loadSpawnPoint();

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("setspawn").setExecutor(new SetSpawnCommand());
        getCommand("setseeker").setExecutor(new SetSeekerCommand());
        getCommand("removeseeker").setExecutor(new RemoveSeekerCommand());
        getCommand("start").setExecutor(new StartCommand());
        getCommand("glowing").setExecutor(new GlowingCommand());

        // PlaceholderAPI support
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderExpansion() {
                @Override
                public @NotNull String getIdentifier() {
                    return "hideandseek";
                }

                @Override
                public @NotNull String getAuthor() {
                    return "Grok";
                }

                @Override
                public @NotNull String getVersion() {
                    return "1.0";
                }

                @Override
                public String onPlaceholderRequest(Player player, @NotNull String identifier) {
                    if (identifier.equals("hider_count")) {
                        return String.valueOf(hiders.size());
                    }
                    if (identifier.equals("seeker_names")) {
                        return seekers.isEmpty() ? "None" : seekers.stream()
                                .map(Player::getName)
                                .collect(Collectors.joining(", "));
                    }
                    if (identifier.equals("hiding_time")) {
                        if (hidingPhase) {
                            long secondsLeft = (hidingTimeEnd - System.currentTimeMillis()) / 1000;
                            if (secondsLeft <= 0) return "0:00";
                            long minutes = secondsLeft / 60;
                            long seconds = secondsLeft % 60;
                            return String.format("%d:%02d", minutes, seconds);
                        }
                        return "0:00";
                    }
                    return null;
                }
            }.register();
        }
    }

    private void loadSpawnPoint() {
        if (getConfig().contains("spawn-point")) {
            double x = getConfig().getDouble("spawn-point.x");
            double y = getConfig().getDouble("spawn-point.y");
            double z = getConfig().getDouble("spawn-point.z");
            float yaw = (float) getConfig().getDouble("spawn-point.yaw");
            float pitch = (float) getConfig().getDouble("spawn-point.pitch");
            String worldName = getConfig().getString("spawn-point.world");
            if (worldName != null && Bukkit.getWorld(worldName) != null) {
                spawnPoint = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
            }
        }
    }

    private void saveSpawnPoint() {
        if (spawnPoint != null) {
            getConfig().set("spawn-point.x", spawnPoint.getX());
            getConfig().set("spawn-point.y", spawnPoint.getY());
            getConfig().set("spawn-point.z", spawnPoint.getZ());
            getConfig().set("spawn-point.yaw", spawnPoint.getYaw());
            getConfig().set("spawn-point.pitch", spawnPoint.getPitch());
            getConfig().set("spawn-point.world", spawnPoint.getWorld().getName());
            saveConfig();
        }
    }

    private class SetSpawnCommand implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Nur Spieler können diesen Befehl ausführen!");
                return true;
            }

            Player player = (Player) sender;
            spawnPoint = player.getLocation();
            saveSpawnPoint();
            player.sendMessage(ChatColor.GREEN + "Spawnpunkt gesetzt und gespeichert!");
            return true;
        }
    }

    private class SetSeekerCommand implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Bitte gib mindestens einen Spielernamen an!");
                return true;
            }

            for (String playerName : args) {
                Player player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    if (!seekers.contains(player)) {
                        seekers.add(player);
                        // Hide seeker's nametag
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tab nametag hideview " + player.getName());
                        player.sendMessage(ChatColor.YELLOW + "Du bist jetzt ein Seeker!");
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + playerName + " ist bereits ein Seeker!");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Spieler " + playerName + " nicht gefunden!");
                }
            }
            return true;
        }
    }

    private class RemoveSeekerCommand implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Bitte gib mindestens einen Spielernamen an!");
                return true;
            }

            for (String playerName : args) {
                Player player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    if (seekers.contains(player)) {
                        seekers.remove(player);
                        // Show seeker's nametag
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tab nametag showview " + player.getName());
                        player.sendMessage(ChatColor.GREEN + "Du bist kein Seeker mehr!");
                        // If game is running, make them a hider
                        if (gameStarted && !hiders.contains(player)) {
                            hiders.add(player);
                            player.setGameMode(GameMode.SURVIVAL);
                            player.teleport(spawnPoint);
                            player.sendMessage(ChatColor.GREEN + "Du bist jetzt ein Hider!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + playerName + " ist kein Seeker!");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Spieler " + playerName + " nicht gefunden!");
                }
            }
            return true;
        }
    }

    private class StartCommand implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (spawnPoint == null) {
                sender.sendMessage(ChatColor.RED + "Bitte setze zuerst einen Spawnpunkt mit /setspawn!");
                return true;
            }

            if (seekers.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "Bitte wähle mindestens einen Seeker mit /setseeker!");
                return true;
            }

            gameStarted = true;
            hidingPhase = true;
            hiders.clear();
            hidingTimeEnd = System.currentTimeMillis() + (HIDING_TIME_SECONDS * 1000);

            // Start hiding phase timer
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!gameStarted || !hidingPhase) {
                        cancel();
                        return;
                    }
                    if (System.currentTimeMillis() >= hidingTimeEnd) {
                        hidingPhase = false;
                        Bukkit.broadcastMessage(ChatColor.AQUA + "Versteckphase vorbei! Seeker können jetzt suchen!");
                        cancel();
                    }
                }
            }.runTaskTimer(HideAndSeek.this, 0L, 20L); // Check every second

            // Set players as hiders or seekers
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!seekers.contains(player)) {
                    hiders.add(player);
                    player.setGameMode(GameMode.SURVIVAL);
                    player.teleport(spawnPoint);
                    player.sendMessage(ChatColor.GREEN + "Du bist ein Hider! Du hast 10 Minuten zum Verstecken!");
                } else {
                    player.setGameMode(GameMode.SURVIVAL);
                    player.teleport(spawnPoint);
                    player.sendMessage(ChatColor.YELLOW + "Du bist ein Seeker! Warte 10 Minuten, bis die Versteckphase vorbei ist!");
                }
            }

            Bukkit.broadcastMessage(ChatColor.AQUA + "Hide and Seek Spiel gestartet! Versteckphase: 10 Minuten");
            return true;
        }
    }

    private class GlowingCommand implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (!gameStarted) {
                sender.sendMessage(ChatColor.RED + "Das Spiel hat noch nicht begonnen!");
                return true;
            }

            for (Player hider : hiders) {
                if (hider.hasPotionEffect(PotionEffectType.GLOWING)) {
                    hider.removePotionEffect(PotionEffectType.GLOWING);
                    sender.sendMessage(ChatColor.GREEN + "Leuchten für Hider deaktiviert!");
                } else {
                    hider.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));
                    sender.sendMessage(ChatColor.GREEN + "Leuchten für Hider aktiviert!");
                }
            }
            return true;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (spawnPoint != null) {
            if (gameStarted) {
                player.setGameMode(GameMode.SPECTATOR);
                player.teleport(spawnPoint);
                player.sendMessage(ChatColor.BLUE + "Das Spiel läuft bereits! Du bist im Zuschauermodus.");
            } else {
                player.setGameMode(GameMode.SURVIVAL);
                player.teleport(spawnPoint);
            }
        }
        // Handle seekers' nametags if they rejoin
        if (gameStarted && seekers.contains(player)) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tab nametag hideview " + player.getName());
            player.sendMessage(ChatColor.YELLOW + "Du bist ein Seeker!");
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!gameStarted) return;

        Player player = event.getEntity();
        if (hiders.contains(player)) {
            player.setGameMode(GameMode.SPECTATOR);
            hiders.remove(player);
            Bukkit.broadcastMessage(ChatColor.RED + player.getName() + " wurde gefunden und ist jetzt im Zuschauermodus!");

            // Check if game is over
            if (hiders.isEmpty()) {
                gameStarted = false;
                hidingPhase = false;
                // Show seekers' nametags
                for (Player seeker : seekers) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tab nametag showview " + seeker.getName());
                }
                seekers.clear();
                Bukkit.broadcastMessage(ChatColor.GOLD + "Das Spiel ist vorbei! Die Seeker haben gewonnen!");
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!gameStarted || hidingPhase) {
            event.setCancelled(true);
            return;
        }

        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();

            // Only seekers can deal damage
            if (!seekers.contains(damager)) {
                event.setCancelled(true);
            }
        }
    }
}