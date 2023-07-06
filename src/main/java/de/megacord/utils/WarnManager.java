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

    private UUID targetUUID;
    private UUID vonUUID;
    private String grund;
    private long timestamp;
    private Configuration settings;
    private DataSource source;


    public WarnManager(UUID targetUUID, UUID vonUUID, String grund, long timestamp, Configuration settings, DataSource source) {
        this.targetUUID = targetUUID;
        this.vonUUID = vonUUID;
        this.grund = grund;
        this.timestamp = timestamp;
        this.settings = settings;
        this.source = source;
    }

    public WarnManager() {


    }

    public void addWarn() {
        new HistoryManager().insertInDB(getTargetUUID(), getVonUUID(), "warn", getGrund(), getTimestamp(), -1, -1, -1);
        String message = (MegaCord.Prefix + getSettings().getString("WarnInfo").replace("%player%", UUIDFetcher.getName(getVonUUID())).replace("%target%", UUIDFetcher.getName(getTargetUUID())).replace("%reason%", getGrund()).replace("&", "§"));
        MegaCord.logger().info(message);

        PlayerData playerData = new PlayerData(targetUUID);
        playerData.updatePlayerData("warnsReceive", null);
        playerData.updatePlayerData("warnsMade", null);

        for (ProxiedPlayer all : ProxyServer.getInstance().getPlayers()) {
            if ((all.hasPermission("megacord.punish.notify") || all.hasPermission("megacord.*") && !all.getName().equalsIgnoreCase(UUIDFetcher.getName(vonUUID))));
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
        PreparedStatement ps = conn.prepareStatement("DELETE FROM history WHERE TargetUUID = ? AND Type = ?");) {
            ps.setString(1, getTargetUUID().toString());
            ps.setString(1, "warn");
            ps.executeUpdate();
        } catch (SQLException e) {
            MegaCord.logger().log(Level.WARNING, "Löschen aller Warns fehlgeschlagen", e);
        }
    }

        public UUID getTargetUUID() {
        return targetUUID;
    }

    public UUID getVonUUID() {
        return vonUUID;
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
