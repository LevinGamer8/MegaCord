package de.megacord.utils;

import de.megacord.MegaCord;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class WarnManager {

    private String targetName;
    private String vonName;
    private String grund;
    private long timestamp;
    private Configuration settings;
    private DataSource source;


    public WarnManager(String targetName, String vonName, String grund, long timestamp, Configuration settings, DataSource source) {
        this.targetName = targetName;
        this.vonName = vonName;
        this.grund = grund;
        this.timestamp = timestamp;
        this.settings = settings;
        this.source = source;
    }

    public WarnManager() {


    }

    public void addWarn() {
        new HistoryManager().insertInDB(targetName, vonName, "warn", getGrund(), getTimestamp(), -1, -1, -1);
        String message = (MegaCord.Prefix + getSettings().getString("WarnInfo").replace("%player%", getVonName()).replace("%target%", getTargetName()).replace("%reason%", getGrund()).replace("&", "§"));
        MegaCord.logger().info(message);

        PlayerData playerData = new PlayerData(targetName);
        playerData.updatePlayerData("warnsReceive", null);
        playerData.updatePlayerData("warnsMade", null);

        for (ProxiedPlayer all : ProxyServer.getInstance().getPlayers()) {
            if ((all.hasPermission("megacord.punish.notify") || all.hasPermission("megacord.*") && !all.getName().equalsIgnoreCase(vonName)));
            all.sendMessage(new TextComponent(message));
        }

    }

    public void deleteWarn(String id) {
        try (Connection conn = source.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM history WHERE Erstellt = ?");) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            MegaCord.logger().log(Level.WARNING, "Warn mit der ID " + id + " konnte nicht gelöscht werden", e);
        }
    }

    public void deleteAllWarns() {
        try (Connection conn = source.getConnection();
        PreparedStatement ps = conn.prepareStatement("DELETE FROM history WHERE TargetName = ? AND Type = ?");) {
            ps.setString(1, getTargetName().toString());
            ps.setString(1, "warn");
            ps.executeUpdate();
        } catch (SQLException e) {
            MegaCord.logger().log(Level.WARNING, "Löschen aller Warns fehlgeschlagen", e);
        }
    }

        public String getTargetName() {
        return targetName;
    }

    public String getVonName() {
        return vonName;
    }

    public String getGrund() {
        return grund;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Configuration getSettings() {
        return settings;
    }

    public void setSource(DataSource source) {
        this.source = source;
    }
}
