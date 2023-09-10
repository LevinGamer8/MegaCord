package de.megacord.commands;

import de.megacord.MegaCord;
import de.megacord.utils.PlayerData;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReportCommand extends Command {

    private DataSource source;


    public ReportCommand() {
        super("report", "megacord.command.report");
        this.source = MegaCord.getInstance().getDataSource();
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent("Dieser Command kann nur von Spielern ausgeführt werden."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(new TextComponent("§4Bitte gib den Namen und den Grund für den Report an."));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;
        String reportedName = args[0];
        String reason = String.join(" ", args[1]);
        PlayerData pl = new PlayerData(reportedName);
        if (!(pl.exists())) {
            sender.sendMessage(new TextComponent("§4Es wurde kein Spieler mit dem Namen '" + reportedName + "' gefunden."));
            return;
        }
        String query = "INSERT INTO reports (reporter, reported, reason) VALUES (?, ?, ?)";
        try (Connection conn = source.getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, player.getName());
                statement.setString(2, reportedName);
                statement.setString(3, reason);
                statement.executeUpdate();
                player.sendMessage(new TextComponent("§6Du §7hast §aerfolgreich §4" + reportedName + " gemeldet."));
                for (ProxiedPlayer team : ProxyServer.getInstance().getPlayers()) {

                    if (team.hasPermission("megacord.punish.notify")) {
                        team.sendMessage(new TextComponent(ChatColor.DARK_GRAY + "-----------------------------------------------------"));
                        team.sendMessage(new TextComponent(ChatColor.GRAY + "Der " + ChatColor.GOLD + "Spieler " + ChatColor.DARK_RED + reportedName + " §7wurde §4reported!\n\n§bGrund §8» §c" + reason + "\n\n§2Reporter §8» §b" + sender.getName()));
                        team.sendMessage(new TextComponent(ChatColor.DARK_GRAY + "-----------------------------------------------------"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(new TextComponent("§4Es ist ein Fehler aufgetreten."));
            return;
        }
    }
}