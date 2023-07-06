package de.megacord.utils;

import de.megacord.MegaCord;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public final class DBUtil {
    public static String prettySQLException(SQLException ex) {
        return "SQLException: " + ex.getMessage() + "\n"
                + "SQLState: " + ex.getSQLState() + "\n"
                + "VendorError: " + ex.getErrorCode();
    }

    public static String rowToString(ResultSet rs) throws SQLException {
        List<String> list = new ArrayList<>();
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            list.add(rs.getString(i));
        }

        return String.join(", ", list);
    }

    public static CompletableFuture<Integer> getWhatCount(DataSource source, UUID player, String type, boolean where) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = source.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS count FROM history WHERE TargetUUID = ?" + (where ? " AND Type = ?" : "") + " ORDER BY ERSTELLT DESC");) {
                ps.setString(1, player.toString());
                if (where) {
                    ps.setString(2, type);
                }
                ResultSet rs = ps.executeQuery();
                while (rs.first())
                    return rs.getInt(1);
            } catch (SQLException e) {
                MegaCord.logger().log(Level.WARNING, "could not count for " + type, e);
            }
            return -1;
        }, MegaCord.getInstance().EXECUTOR_SERVICE);
    }

    public static CompletableFuture<Boolean> timeExists(DataSource source, long erstellt) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = source.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM bannedPlayers WHERE TimeStamp = ?")) {
                ps.setLong(1, erstellt);
                ResultSet rs = ps.executeQuery();
                return rs.next();
            } catch (SQLException e) {
                MegaCord.logger().log(Level.WARNING, "could not check if timestamp exists", e);
            }
            return false;
        }, MegaCord.getInstance().EXECUTOR_SERVICE);
    }
}
