package de.megacord.listener;

import de.megacord.MegaCord;
import de.megacord.utils.Onlinezeit;
import de.megacord.utils.PlayerData;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public class PlayerDisconnectListener implements Listener {


    private DataSource source;

    public PlayerDisconnectListener(Plugin plugin, DataSource source) {
        this.source = source;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent e) {
        LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Europe/Berlin"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        new Onlinezeit(e.getPlayer().getName(), date.format(formatter), source).leave();
        try (Connection conn = source.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE playerdata SET lastOnline = ? WHERE Name = ?")) {
            ps.setLong(1, System.currentTimeMillis());
            ps.setString(2, e.getPlayer().getName());
            ps.executeUpdate();
        } catch (SQLException e1) {
            MegaCord.logger().log(Level.WARNING, "cloud not update lastonline for a player", e1);
        }
        PlayerData pl = new PlayerData(e.getPlayer().getName());
        pl.setIPOnlinePlayers(e.getPlayer().getAddress().getAddress().getHostAddress(), pl.getIPOnlinePlayers(e.getPlayer().getAddress().getAddress().getHostAddress()) - 1);
    }

}
