package de.megacord.listener;

import de.megacord.MegaCord;
import de.megacord.utils.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import org.geysermc.floodgate.api.FloodgateApi;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public class PlayerJoinListener implements Listener {

    private DataSource source;
    private Configuration settings;
    private Configuration standardBans;
    private Plugin plugin;

    private final HashMap<ProxiedPlayer, ProxiedPlayer> onlinePlayers = new HashMap<>();

    public PlayerJoinListener(Plugin plugin, DataSource source, Configuration settings, Configuration standardBans) {
        this.source = source;
        this.settings = settings;
        this.standardBans = standardBans;
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onLogin(LoginEvent e) throws SQLException {
        PendingConnection con = e.getConnection();
        String name = con.getName();
            new PlayerData(name).createPlayer(e.getConnection().getAddress().getAddress().getHostAddress(), e.getConnection().getName());
            BanUtils ban = new BanUtils(e.getConnection().getUniqueId().toString(), e.getConnection().getSocketAddress().toString().replace("/", "").split(":")[0], source, settings, standardBans);
            ban.isBanned(name).whenComplete((result, ex) -> {
                ban.containsIP().whenComplete((ipResult, exception) -> {
                    if ((result && ban.getBan() == 1) || ipResult == 1) {
                        ArrayList<String> banArray = new ArrayList<>();
                        int i = 1;
                        banArray.add(MegaCord.fehler + "Du wurdest IP gebannt!\n" + MegaCord.normal + "IP: " + MegaCord.herH + ban.getIp());
                        while (true) {
                            try {
                                String line = ChatColor.translateAlternateColorCodes('&', settings.getString("BanMessage.line" + i)).replace("%von%", ban.getVonName()).replace("%grund%", ban.getGrund()).replace("%bis%", (ban.getBis()) == -1 ? MegaCord.fehler + "Permanent" : MegaCord.formatTime(ban.getBis())).replace("%beweis%", ban.getBeweis() == null ? "/" : ban.getBeweis());
                                banArray.add(line);
                                i++;
                                if (i > settings.getInt("BanMessage.lines")) {
                                    banArray.remove(0);
                                    break;
                                }
                            } catch (Exception e1) {
                                MegaCord.logger().log(Level.WARNING, "could not create ban message", e);
                                break;
                            }
                        }
                        if (banArray.size() == 1) {
                            BanUtils altAccountBan = new BanUtils(e.getConnection().getUniqueId().toString(), null, source, settings, standardBans);
                            altAccountBan.banByStandard(1, e.getConnection().getSocketAddress().toString().replace("/", "").split(":")[0]);
                            e.getConnection().disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', settings.getString("Ban.Disconnectmessage").replace("%reason%", altAccountBan.getGrund()).replace("%absatz%", "\n"))));
                            return;
                        }
                        e.getConnection().disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', String.join("\n", banArray))));
                    }
                });
            });
            LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Europe/Berlin"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            new Onlinezeit(name, date.format(formatter), source).createNew(e.getConnection().getName());
            clearMessages();
            updateBans();
            updateIP(name, e.getConnection().getAddress().getAddress().getHostAddress());
        PlayerData pl = new PlayerData(name);
        pl.setIPOnlinePlayers(con.getAddress().getAddress().getHostAddress(), pl.getIPOnlinePlayers(con.getAddress().getAddress().getHostAddress()) + 1);
             if ((pl.getIPOnlinePlayers(con.getAddress().getAddress().getHostAddress()) > pl.getMaxIP(con.getAddress().getAddress().getHostAddress()))) {
                  e.setCancelled(true);
                  con.disconnect("§3MegaCord §4AntiAlts \n\n §bMelde ich sofort im Support§4!!!\n\n §9Discord: §bhttps://dc-megacraft.de.cool/ \n\n §eID§7: §35896" + pl.getMaxIP(con.getAddress().getAddress().getHostAddress()));
                 }
        if (e.isCancelled()) {
            pl.setIPOnlinePlayers(con.getAddress().getAddress().getHostAddress(), pl.getIPOnlinePlayers(con.getAddress().getAddress().getHostAddress()) - 1);
        }
    }



    private void updateBans() {
        try (Connection conn = source.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM bannedPlayers")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getLong("Bis") != -1L) {
                    long bis = rs.getLong("Bis");
                    if (System.currentTimeMillis() > bis) {
                        new BanUtils(rs.getString("TargetName"), null, source, settings, standardBans).unban(false, "PLUGIN (expired)");
                    }
                }
            }
        } catch (SQLException e) {
            MegaCord.logger().log(Level.WARNING, "could not clean up the bans", e);
        }
    }

    private void updateIP(String name, String ip) {
        ip = ip.replace("/", "").split(":")[0];
        try (Connection conn = source.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE bannedPlayers SET ip = ? WHERE TargetName = ?")) {
            ps.setString(1, ip);
            ps.setString(2, name);
            ps.executeUpdate();
            new PlayerData(name).updatePlayerData("lastIP", ip);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearMessages() {
        try (Connection conn = source.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT timestamp FROM chat");
             PreparedStatement ps1 = conn.prepareStatement("DELETE FROM chat WHERE timestamp = ?")) {
            ResultSet rs = ps.executeQuery();
            long currentTime = System.currentTimeMillis();
            long last15Min = currentTime - 3600000;
            while (rs.next()) {
                if (rs.getLong("timestamp") < last15Min) {
                    ps1.setLong(1, rs.getLong("timestamp"));
                    ps1.executeUpdate();
                }
            }
        } catch (SQLException e) {
            MegaCord.logger().log(Level.WARNING, "could not clear messages", e);
        }
    }


}
