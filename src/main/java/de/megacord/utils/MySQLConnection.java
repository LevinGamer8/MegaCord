package de.megacord.utils;

import de.megacord.MegaCord;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLConnection {
    private static final String HOST = Config.mysqlConfig.getString("host");
    private static final String PORT = Config.mysqlConfig.getString("port");
    private static final String DATABASE = Config.mysqlConfig.getString("database");
    private static final String USERNAME = Config.mysqlConfig.getString("username");
    private static final String PASSWORD = Config.mysqlConfig.getString("password");
    private static final String CONNECTION_STRING = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?user=" + USERNAME + "&password=" + PASSWORD;

    private MySQLConnection() {
        // prevent instantiation
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_STRING);
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void update(String query) {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet executeQuery(String query) {
        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            return statement.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
