package de.megacord.utils;

import de.megacord.MegaCord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class HistoryManager {

    public HistoryManager() {
    }

    public void insertInDB(String targetName, String vonName, String type, String grund, long erstellt, long bis, int perma, int ban) {
        try (Connection conn = MegaCord.getInstance().getDataSource().getConnection(); PreparedStatement createReport = conn.prepareStatement("INSERT INTO history (TargetName,VonName,Type,Grund,Erstellt,Bis,Perma,Ban) VALUES(?,?,?,?,?,?,?,?)");) {
            createReport.setString(1, targetName);
            createReport.setString(2, vonName);
            createReport.setString(3, type);
            createReport.setString(4, grund);
            createReport.setLong(5, erstellt);
            createReport.setLong(6, bis == -1 ? -2 : bis);
            createReport.setInt(7, perma == -1 ? -2 : perma);
            createReport.setInt(8, ban == -1 ? -2 : ban);
            createReport.executeUpdate();
        } catch (SQLException e) {
            MegaCord.logger().log(Level.WARNING, "cannot insert report into db", e);
        }
    }

    public List<HistoryElement> readHistory(String target, int limit, int page, String what, boolean lastOnes) {
        List<HistoryElement> reports = new LinkedList<>();
        page = page * 10 - 10;
        String sql = "SELECT * FROM history " + (target == null ? "" : "WHERE TargetUUID = ?") + (lastOnes ? "" : " AND Type = ? ") + "ORDER BY Erstellt DESC LIMIT ? OFFSET ?";
        try (Connection conn = MegaCord.getInstance().getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (target != null)
                ps.setString(1, target);
            if (!lastOnes)
                ps.setString(2, what);
            ps.setInt(lastOnes ? (target == null ? 1 : 2) : 3, limit);
            ps.setInt(lastOnes ? (target == null ? 2 : 3) : 4, page);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                reports.add(new HistoryElement(rs.getString("TargetName"), rs.getString("VonName"), rs.getString("Type"), rs.getString("Grund"), rs.getLong("Erstellt"), rs.getLong("Bis"), rs.getInt("Perma"), rs.getInt("Ban"), rs.getString("VonEntbannt")));
            }
        } catch (SQLException e) {
            MegaCord.logger().log(Level.WARNING, "could net read history elmts", e);
            return null;
        }
        return reports;
    }
}