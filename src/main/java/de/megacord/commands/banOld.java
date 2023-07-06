package de.megacord.commands;

import de.megacord.utils.MySQLConnection;
import de.megacord.utils.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

public class banOld extends Command {

    private Date endDate;
    private long duration;
    private DataSource source;

    public banOld() {
        super("ban", "megacord.punish.ban");
    }

    private String formatDuration(long duration) {
        if (duration == -1) {
            return "Permanent";
        } else if (duration < 60 * 1000) {
            return duration / 1000 + " Sekunden";
        } else if (duration < 60 * 60 * 1000) {
            long minutes = duration / (60 * 1000);
            return minutes + " " + (minutes == 1 ? "Minute" : "Minuten");
        } else if (duration < 24 * 60 * 60 * 1000) {
            long hours = duration / (60 * 60 * 1000);
            long minutes = (duration % (60 * 60 * 1000)) / (60 * 1000);
            return hours + " " + (hours == 1 ? "Stunde" : "Stunden") + " und " + minutes + " " + (minutes == 1 ? "Minute" : "Minuten");
        } else if (duration < 7 * 24 * 60 * 60 * 1000) {
            long days = duration / (24 * 60 * 60 * 1000);
            long hours = (duration % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
            return days + " " + (days == 1 ? "Tag" : "Tage") + " und " + hours + " " + (hours == 1 ? "Stunde" : "Stunden");
        } else if (duration < 30 * 24 * 60 * 60 * 1000) {
            long weeks = duration / (7 * 24 * 60 * 60 * 1000);
            long days = (duration % (7 * 24 * 60 * 60 * 1000)) / (24 * 60 * 60 * 1000);
            long hours = (duration % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
            return weeks + " " + (weeks == 1 ? "Woche" : "Wochen") + ", " + days + " " + (days == 1 ? "Tag" : "Tage") + " und " + hours + " " + (hours == 1 ? "Stunde" : "Stunden");
        } else if (duration < 365 * 24 * 60 * 60 * 1000) {
            long months = duration / (30 * 24 * 60 * 60 * 1000);
            long weeks = (duration % (30 * 24 * 60 * 60 * 1000)) / (7 * 24 * 60 * 60 * 1000);
            long days = (duration % (7 * 24 * 60 * 60 * 1000)) / (24 * 60 * 60 * 1000);
            return months + " " + (months == 1 ? "Monat" : "Monate") + ", " + weeks + " " + (weeks == 1 ? "Woche" : "Wochen") + ", " + days + " " + (days == 1 ? "Tag" : "Tage");
        }
        return formatDuration(duration);
    }


    private long parseDuration (String durationString) throws IllegalArgumentException {
        long duration;
        if (durationString.equals("perma")) {
            duration = -1;
        } else if (durationString.matches("^\\d+$")) {
            duration = Long.parseLong(durationString) * 60 * 1000;
        } else if (durationString.matches("^\\d+[mhdwMy]$")) {
            char unit = durationString.charAt(durationString.length() - 1);
            long quantity = Long.parseLong(durationString.substring(0, durationString.length() - 1));
            switch (unit) {
                case 'm':
                    duration = quantity * 60 * 1000;
                    break;
                case 'h':
                    duration = quantity * 60 * 60 * 1000;
                    break;
                case 'd':
                    duration = quantity * 24 * 60 * 60 * 1000;
                    break;
                case 'w':
                    duration = quantity * 7 * 24 * 60 * 60 * 1000;
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalArgumentException();
        }
        return duration;
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender.hasPermission("megacord.punish.ban")) {
            if (args.length <= 2) {
                sender.sendMessage("Nutzung: /ban <Spieler> <Zeit> <Grund>");
                return;
            }
            String playerName = args[0];
            String durationString = args[1];
            UUID uuid = null;
            uuid = UUIDFetcher.getUUID(playerName);
            String grund = args[2];

            long dauer;
            try {
                dauer = parseDuration(durationString);
            } catch (IllegalArgumentException e) {
                sender.sendMessage("Bitte gebe immer eine Zahl und eine Zeitangabe an!\n Bsp. 5h = 5 Stunden\n m = Minute(n); h = Stunde(n); d = Tag(e); w = Woche(n) perma = permanent.");
                return;
            }

            if (uuid != null) {

                String query = "INSERT INTO bans (uuid, spielername, grund, operator, dauer, banende) VALUES (?, ?, ?, ?, ?, ?)";
                try (Connection connection = MySQLConnection.getConnection()) {
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setString(1, uuid.toString());
                        statement.setString(2, playerName);
                        statement.setString(3, grund);
                        statement.setString(4, sender.getName());
                        statement.setString(5, String.valueOf(dauer));
                        statement.setLong(6, System.currentTimeMillis() + dauer);
                        statement.executeUpdate();
                        for (ProxiedPlayer team : ProxyServer.getInstance().getPlayers()) {
                            if (team.hasPermission("megacord.punish.notify")) {
                                team.sendMessage(new TextComponent(ChatColor.DARK_GRAY + "-----------------------------------------------------"));
                                team.sendMessage(new TextComponent(ChatColor.GRAY + "Der " + ChatColor.GOLD + "Spieler " + ChatColor.DARK_RED + playerName + " §7wurde §4gebannt!\n\n§bGrund §8» §c" + grund + "\n\n§2Dauer §8» §b" + durationString));
                                team.sendMessage(new TextComponent(ChatColor.DARK_GRAY + "-----------------------------------------------------"));
                            }
                        }
                        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerName);
                        String bannDauer = formatDuration(dauer);
                        if (player != null) {
                            player.disconnect(new TextComponent("§8-------------------------------\n\n §3§lMegaCraft §e§lNetzwerk§r\n\n§4Du wurdest §4§lgebannt§!§r\n\n§bGrund §8» §c" + grund + "\n\n" + "§2Dauer §8» §b" + bannDauer + "\n\n§8-------------------------------"));
                        }

                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }


            } else {
                sender.sendMessage(new TextComponent("§4Der Spieler wurde nicht in der Datenbank gefunden!\n Überprüfe, ob du den Namen richtig eingegeben hast."));
            }


        }

    }
}