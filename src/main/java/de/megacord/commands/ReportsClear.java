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

public class ReportsClear extends Command {

    public ReportsClear() {
        super("reportsclear", "megacord.command.reportsclear");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent("Dieser Command kann nur von Spielern ausgeführt werden."));
            return;
        }
        try (Connection connection = MySQLConnection.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM reports");
                int rowsAffected = statement.executeUpdate();
            sender.sendMessage(ChatColor.GREEN + "Es wurden alle §4Reports §agelöscht§7.");
            for (ProxiedPlayer team : ProxyServer.getInstance().getPlayers()) {
                if (team.hasPermission("megacord.punish.notify")) {
                    team.sendMessage(new TextComponent(ChatColor.DARK_GRAY + "-----------------------------------------------------"));
                    team.sendMessage(new TextComponent("§6" + sender + ChatColor.GREEN + " §ahat alle " + ChatColor.DARK_RED + "Reports " + ChatColor.GREEN + "gelöscht§7."));
                    team.sendMessage(new TextComponent(ChatColor.DARK_GRAY + "-----------------------------------------------------"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "Ein Fehler ist aufgetreten.");
        }
    }
}