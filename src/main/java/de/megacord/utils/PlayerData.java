package de.megacord.utils;

import de.megacord.MegaCord;

import javax.sql.DataSource;
import java.net.SocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class PlayerData {

    private UUID uuid;
    private String name;
    private String firstip;
    private String lastip;
    private long firstjoin;
    private long lastonline;
    private int bansMade;
    private int warnsMade;
    private int reportsMade;
    private int bansReceive;
    private int warnsReceive;


    Map<String, Integer> ipOnlinePlayers = new HashMap<>();
    private DataSource source;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.source = MegaCord.getInstance().getDataSource();
        loadData();
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstip() {
        return firstip;
    }

    public void setFirstip(String firstip) {
        this.firstip = firstip;
    }

    public String getLastip() {
        return lastip;
    }

    public void setLastip(String lastip) {
        this.lastip = lastip;
    }

    public long getFirstjoin() {
        return firstjoin;
    }

    public void setFirstjoin(long firstjoin) {
        this.firstjoin = firstjoin;
    }

    public long getLastonline() {
        return lastonline;
    }

    public void setLastonline(long lastonline) {
        this.lastonline = lastonline;
    }

    public int getBansMade() {
        return bansMade;
    }

    public void setBansMade(int bansMade) {
        this.bansMade = bansMade;
    }

    public int getWarnsMade() {
        return warnsMade;
    }

    public void setWarnsMade(int warnsMade) {
        this.warnsMade = warnsMade;
    }

    public int getReportsMade() {
        return reportsMade;
    }

    public void setReportsMade(int reportsMade) {
        this.reportsMade = reportsMade;
    }

    public int getBansReceive() {
        return bansReceive;
    }

    public void setBansReceive(int bansReceive) {
        this.bansReceive = bansReceive;
    }

    public int getWarnsReceive() {
        return warnsReceive;
    }

    public void setWarnsReceive(int warnsReceive) {
        this.warnsReceive = warnsReceive;
    }


    public void loadData() {
        try (Connection conn = source.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM playerdata WHERE UUID = ?")) {
            ps.setString(1, getUuid().toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                setName(rs.getString("Name"));
                setFirstip(rs.getString("firstIP"));
                setLastip(rs.getString("lastIP"));
                setFirstjoin(rs.getLong("firstJoin"));
                setLastonline(rs.getLong("lastOnline"));
                setBansMade(rs.getInt("bansMade"));
                setWarnsMade(rs.getInt("warnsMade"));
                setReportsMade(rs.getInt("reportsMade"));
                setBansReceive(rs.getInt("bansReceive"));
                setWarnsReceive(rs.getInt("warnsReceive"));
                setMaxIP(getLastip(), rs.getInt("maxIP"));
                setIPOnlinePlayers(getLastip(), rs.getInt("ipOnlinePlayers"));
            }
        } catch (SQLException e) {
            MegaCord.logger().log(Level.WARNING, "could not load playerdata", e);
        }
    }


    public void createPlayer(UUID target, String ip, String name) {
        setUuid(target);
        setName(name);
        try (Connection conn = source.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO playerdata (UUID,Name,firstIP,lastIP,firstJoin,lastOnline,bansMade,warnsMade,reportsMade,bansReceive,warnsReceive,maxIP,ipOnlinePlayers) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE UUID=UUID")) {
            ps.setString(1, getUuid().toString()); // uuid
            ps.setString(2, getName()); // name
            ps.setString(3, ip.replace("/", "").split(":")[0]);
            ps.setString(4, ip == null ? null : ip.replace("/", "").split(":")[0]); // lastIP
            ps.setLong(5, System.currentTimeMillis()); // firstJoin
            ps.setLong(6, -1); // lastOnline
            ps.setInt(7, 0); // bansMade
            ps.setInt(8, 0); // warnsMade
            ps.setInt(9, 0); // reportsMade
            ps.setInt(10, 0); //  bansReceived
            ps.setInt(11, 0); // warnsReceived
            ps.setInt(12, 1); //maxAccountPerIP
            ps.setInt(13, 0); //ipOnlinePlayers
            ps.executeUpdate();
        } catch (SQLException e) {
            MegaCord.logger().log(Level.WARNING, "could not create playerdata", e);
        }
    }

    public void updatePlayerData(String what, String ip) {
        String value = null;
        switch (what.toLowerCase()) {
            case "bansmade":
                value = String.valueOf((this.getBansMade() + 1));
                break;
            case "warnsmade":
                value = String.valueOf((this.getWarnsMade() + 1));
                break;
            case "reportsmade":
                value = String.valueOf((this.getReportsMade() + 1));
                break;
            case "bansreceive":
                value = String.valueOf((this.getBansReceive() + 1));
                break;
            case "warnsreceive":
                value = String.valueOf((this.getWarnsReceive() + 1));
                break;
            case "lastip":
                value = ip;
                break;
        }

        try (Connection conn = source.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE playerdata SET " + what + " = ? WHERE UUID = ?")) {
            ps.setString(1, value);
            ps.setString(2, this.getUuid().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            MegaCord.logger().log(Level.WARNING, "could not update playerdata", e);
        }
    }

    public void saveBedrockUser(UUID uuid, String username) throws SQLException {
        try (Connection conn = source.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO bedrockPlayers (UUID, Name) VALUES(?,?) ON DUPLICATE KEY UPDATE UUID=UUID")) {
            ps.setString(1, String.valueOf(uuid));
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            MegaCord.logger().log(Level.WARNING, "could not update playerdata", e);
        }
    }


    public String getSavedBedrockUsername(UUID player) throws SQLException, ExecutionException, InterruptedException {
        try (Connection conn = source.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM bedrockPlayers WHERE UUID = " + player)) {
            ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            return rs.getString("username");
        }
        return null;
    }
    }

    public UUID getSavedBedrockUUID(String username) throws SQLException, ExecutionException, InterruptedException {
        try (Connection conn = source.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM bedrockPlayers WHERE username = " + username)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                return UUID.fromString(rs.getString("uuid"));
            }
            return null;
        }
    }
    public boolean isSavedBedrockPlayer(UUID player) throws SQLException, ExecutionException, InterruptedException {
        try (Connection conn = source.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM bedrockPlayers WHERE uuid = ?")) {
            ps.setString(1, player.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    public boolean isSavedBedrockPlayer(String username) throws SQLException, ExecutionException, InterruptedException {
            try (Connection conn = source.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM bedrockPlayers WHERE username = ?" )) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    return true;
                }
                return false;
            }
        }

    public int getMaxIP(String address) throws SQLException {
        try (Connection conn = source.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM playerdata WHERE lastIP = ?" )) {
            ps.setString(1, address);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                return (rs.getInt("maxIP"));
            }
        }
        return 1;
    }

    public void setMaxIP(String address, int maxIP) throws SQLException {
             try (Connection conn = source.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE playerdata SET maxIP = ? WHERE lastIP = ?")) {
                 ps.setInt(1, maxIP);
                 ps.setString(2, address);
                 ps.executeUpdate();
        }
    }

    public int getIPOnlinePlayers(String hostAddress) {
        try (Connection conn = source.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM playerdata WHERE lastIP = ?" )) {
            ps.setString(1, hostAddress);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                return (rs.getInt("ipOnlinePlayers"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public void setIPOnlinePlayers(String hostAddress, int onlinePlayers) {
        ipOnlinePlayers.put(hostAddress, onlinePlayers);
        try (Connection conn = source.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE playerdata SET ipOnlinePlayers = ? WHERE lastIP = ?")) {
            ps.setInt(1, onlinePlayers);
            ps.setString(2, hostAddress);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
