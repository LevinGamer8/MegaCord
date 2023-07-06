package de.megacord.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.megacord.MegaCord;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

public class ConnectionPoolFactory {

    private Configuration config;
    private Map<Class<? extends Plugin>, HikariDataSource> dataPools = new HashMap<>();

    public ConnectionPoolFactory(Configuration config) {
        this.config = config;
    }

    public DataSource getPluginDataSource(Plugin plugin) throws SQLException {
        if (dataPools.containsKey(plugin.getClass())) {
            return dataPools.get(plugin.getClass());
        }

        String port = String.valueOf(config.getInt("port"));

        Properties props = new Properties();
        props.setProperty("dataSourceClassName", "org.mariadb.jdbc.MariaDbDataSource");
        props.setProperty("dataSource.serverName", config.getString("host"));
        props.setProperty("dataSource.portNumber", port);
        props.setProperty("dataSource.user", config.getString("username"));
        props.setProperty("dataSource.password", config.getString("password"));
        props.setProperty("dataSource.databaseName", config.getString("database"));

        HikariConfig hikariConfig = new HikariConfig(props);
        hikariConfig.setMaximumPoolSize(10);

        HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        dataPools.computeIfAbsent(plugin.getClass(), k -> new HikariDataSource(hikariConfig));

        try (Connection conn = dataSource.getConnection()) {
            conn.isValid(5 * 1000);
        } catch (SQLException e) {
            MegaCord.logger().log(Level.WARNING, "Invalid data for data source. Could not connect.\n" + DBUtil.prettySQLException(e), e);
            dataPools.remove(plugin.getClass());
            throw e;
        }

        MegaCord.logger().info("Verbindung für MegaCord erstellt.");

        return dataSource;
    }

    public void shutdown() {
        for (HikariDataSource value : dataPools.values()) {
            value.close();
        }
        MegaCord.logger().info("Verbindung erfolgreich geschlossen");
    }
}
