package de.megacord.commands;

import de.megacord.utils.MySQLConnection;
import de.megacord.utils.PlayerData;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ReportsCommand extends Command {

    public ReportsCommand() {
        super("reports", "megacord.command.reports");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent("Dieser Command kann nur von Spielern ausgeführt werden."));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;
        String query = "SELECT * FROM reports";
        try (Connection connection = MySQLConnection.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        UUID reporterUuid = UUID.fromString(resultSet.getString("reporter"));
                        UUID reportedUuid = UUID.fromString(resultSet.getString("reported"));
                        String reason = resultSet.getString("reason");

                        //String reporterData = PlayerData.getData(reporterUuid);
                        //String reportedData = PlayerData.getData(reportedUuid);

                        //String reporterName = reporterData.getName();
                        //String reportedName = reportedData.getName();

                        //player.sendMessage(new TextComponent("§7Reporter: §a" + reporterName + "§8 | §7Reported: §4" + reportedName + "§8 | §7Grund: §b" + reason));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(new TextComponent("§4Es ist ein Fehler aufgetreten."));
        }
    }
}