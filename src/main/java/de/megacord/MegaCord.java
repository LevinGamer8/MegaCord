package de.megacord;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.megacord.commands.BungeeCommand;
import de.megacord.utils.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import javax.sql.DataSource;

public final class MegaCord extends Plugin {

    private Timer timer;
    private String message;

    private ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, Object> config = new HashMap();
    private static MegaCord plugin;
    private DataSource dataSource;
    private HashMap<String, Long> allOnlineTimeToday = new HashMap<>();
    private HashMap<ProxiedPlayer, ProxiedPlayer> activechats = new HashMap<>();
    private static final ConcurrentMap<String, Integer> ipCounts = new ConcurrentHashMap<>();
    public static ConcurrentMap<String, Integer> getIpCounts() { return MegaCord.ipCounts; }
    public ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();


    public DataSource getDataSource() {
        return dataSource;
    }
    public static Logger logger() {
        return plugin.getLogger();
    }

    public static String Prefix = "&3MegaCraft&7: &r";
    public static String noPerm = "&4Du hast keine Rechte!";
    public static String normal = "&a";
    public static String fehler = "&c";
    public static String herH = "&b";
    public static String other = "&e";
    public static String other2 = "&7";


    public void onEnable() {

        switch (ProxyServer.getInstance().getName()) {

            case "Waterfall":
                getLogger().log(Level.INFO, "MegaCord running on Waterfall");
                break;
            case "Bungeecord":
                getLogger().log(Level.INFO, "MegaCord running on Bungeecord");
                break;
            case "MegaCord":
                specialFeatures();
                getLogger().log(Level.INFO, "MegaCord running on MegaCord (: Special Features werden aktiviert!");
        }

        plugin = this;
        getLogger().log(Level.INFO, "MegaCord Plugin wird geladen");
        getLogger().log(Level.INFO, "Configs werden geladen");

        loadConfig();

        try {

        normal = de.megacord.utils.Config.settings.getString("ChatColor.normal").replace("&", "§");
        fehler = de.megacord.utils.Config.settings.getString("ChatColor.fehler").replace("&", "§");
        herH = de.megacord.utils.Config.settings.getString("ChatColor.hervorhebung").replace("&", "§");
        other = de.megacord.utils.Config.settings.getString("ChatColor.other").replace("&", "§");
        other2 = de.megacord.utils.Config.settings.getString("ChatColor.other2").replace("&", "§");
        Prefix = de.megacord.utils.Config.settings.getString("Prefix").replace("&", "§") + normal;
        noPerm = de.megacord.utils.Config.settings.getString("NoPerm").replace("&", "§");

        } catch (NullPointerException e) {
        getLogger().log(Level.WARNING, "Some messages not found!", e);
        }


        ConnectionPoolFactory connectionPool = new ConnectionPoolFactory(de.megacord.utils.Config.mysqlConfig);

        try {
            dataSource = connectionPool.getPluginDataSource(this);
        } catch (SQLException e) {
            logger().log(Level.SEVERE, "Datenbankverbindung fehlgeschlagen", e);
            getProxy().getPluginManager().unregisterListeners(this);
            getProxy().getPluginManager().unregisterCommands(this);
            onDisable();
            return;
        }
        Registry();
        initMySQL();
    }

    private void Registry() {
        Registry registry = new Registry(this, dataSource, de.megacord.utils.Config.settings, de.megacord.utils.Config.blacklist, de.megacord.utils.Config.standardBans, activechats);
        registry.registerCommands();
        registry.registerListeners();
    }

    private void loadConfig() {
        Config config = new Config(plugin);
        config.loadConfig();
    }

    public void initMySQL() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS bannedPlayers (TargetName VARCHAR(64),VonName VARCHAR(64),Grund VARCHAR(100) NOT NULL,TimeStamp BIGINT NOT NULL,Bis VARCHAR(100) NOT NULL,Perma TINYINT(1) NOT NULL,Ban TINYINT(1) NOT NULL, ip VARCHAR(100), baneditiertvon VARCHAR(36), beweis VARCHAR(200))");
             PreparedStatement ps1 = conn.prepareStatement("CREATE TABLE IF NOT EXISTS history (TargetName VARCHAR(64), VonName VARCHAR(64), Type VARCHAR(50), Grund VARCHAR(100), Erstellt BIGINT(8), Bis BIGINT(8), Perma TINYINT(1), Ban TINYINT(1), VonEntbannt VARCHAR(20))");
             PreparedStatement ps2 = conn.prepareStatement("CREATE TABLE IF NOT EXISTS playerdata (Name VARCHAR(64) NOT NULL, firstIP VARCHAR(60), lastIP VARCHAR(60), firstJoin BIGINT(8) NOT NULL, lastOnline BIGINT(8), bansMade INT(60) NOT NULL DEFAULT 0, warnsMade INT(60) NOT NULL DEFAULT 0, reportsMade INT(60) NOT NULL DEFAULT 0, bansReceive INT(60) NOT NULL DEFAULT 0, warnsReceive INT(60) NOT NULL DEFAULT 0, maxIP INT(2), ipOnlinePlayers INT)");
             PreparedStatement ps3 = conn.prepareStatement("CREATE TABLE IF NOT EXISTS chat (message VARCHAR(255), uuid VARCHAR(100), timestamp BIGINT(8), server VARCHAR(50))");
             PreparedStatement ps4 = conn.prepareStatement("CREATE TABLE IF NOT EXISTS onlinetime (Name VARCHAR(100), Datum VARCHAR(50), onlinezeit BIGINT(8))");
        ) {
            ps.executeUpdate();
            ps1.executeUpdate();
            ps2.executeUpdate();
            ps3.executeUpdate();
            ps4.executeUpdate();
        } catch (SQLException e) {
            logger().log(Level.SEVERE, "Keine Verbindung zur Datenbank!", e);
        }
    }

    public HashMap<String, Long> getAllOnlineTimeToday() {
        return allOnlineTimeToday;
    }

    public static String formatTime(Long timestamp) {
        LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("Europe/Berlin"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm");
        return date.format(formatter) + " Uhr";
    }

    public void specialFeatures() {
        this.getProxy().getPluginManager().unregisterCommand(new BungeeCommand());


    }

    public void onDisable() {
        getLogger().log(Level.INFO, "MegaCord Plugin wird deaktiviert");
    }

    public static MegaCord getInstance() {
        return plugin;
    }
}

