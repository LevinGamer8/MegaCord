package de.megacord.listener;

import de.megacord.MegaCord;
import de.megacord.utils.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

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
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class PlayerJoinListener implements Listener {

    private DataSource source;
    private Configuration settings;
    private Configuration standardBans;
    private Plugin plugin;

    private HashMap<ProxiedPlayer, ProxiedPlayer> onlinePlayers = new HashMap<>();

    private final HashMap<String, ImageMessage> head_cache = new HashMap<>();

    public PlayerJoinListener(Plugin plugin, DataSource source, Configuration settings, Configuration standardBans) {
        this.source = source;
        this.settings = settings;
        this.standardBans = standardBans;
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onLogin(LoginEvent e) throws SQLException, ExecutionException, InterruptedException {
        PendingConnection con = e.getConnection();
        UUID uuid = con.getUniqueId();
        PlayerData playerData = new PlayerData(uuid);
        if(UUIDFetcher.getName(uuid) == null && !playerData.isSavedBedrockPlayer(uuid)) {
            if(FloodgateApi.getInstance().getPlayer(uuid) != null) {
                FloodgatePlayer floodgatePlayer = FloodgateApi.getInstance().getPlayer(uuid);
                playerData.saveBedrockUser(floodgatePlayer.getJavaUniqueId(), con.getName());
                new PlayerData(uuid).createPlayer(uuid, con.getAddress().getAddress().getHostAddress(), floodgatePlayer.getJavaUsername());
                updateIP(floodgatePlayer.getJavaUniqueId(), con.getAddress().getAddress().getHostAddress());
            }
        }
            UUID target = e.getConnection().getUniqueId();
            new PlayerData(target).createPlayer(target, e.getConnection().getSocketAddress().toString(), e.getConnection().getName());
            BanUtils ban = new BanUtils(e.getConnection().getUniqueId(), e.getConnection().getSocketAddress().toString().replace("/", "").split(":")[0], source, settings, standardBans);
            ban.isBanned().whenComplete((result, ex) -> {
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
                            BanUtils altAccountBan = new BanUtils(e.getConnection().getUniqueId(), null, source, settings, standardBans);
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
            new Onlinezeit(target, date.format(formatter), source).createNew(e.getConnection().getName());
            UUIDFetcher.getName(e.getConnection().getUniqueId());
            UUIDFetcher.getUUID(UUIDFetcher.getName(e.getConnection().getUniqueId()));
            clearMessages();
            updateBans();
            updateIP(target, e.getConnection().getAddress().getAddress().getHostAddress());

        }

    private void updateBans() {
        try (Connection conn = source.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM bannedPlayers")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getLong("Bis") != -1L) {
                    long bis = rs.getLong("Bis");
                    if (System.currentTimeMillis() > bis) {
                        new BanUtils(UUIDFetcher.getUUID(rs.getString("TargetName")), null, source, settings, standardBans).unban(false, "PLUGIN (expired)");
                    }
                }
            }
        } catch (SQLException e) {
            MegaCord.logger().log(Level.WARNING, "could not clean up the bans", e);
        }
    }

    private void updateIP(UUID uuid, String ip) {
        ip = ip.replace("/", "").split(":")[0];
        try (Connection conn = source.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE bannedPlayers SET ip = ? WHERE TargetUUID = ?")) {
            ps.setString(1, ip);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            new PlayerData(uuid).updatePlayerData("lastIP", ip);
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
