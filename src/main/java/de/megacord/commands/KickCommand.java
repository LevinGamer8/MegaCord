package de.megacord.commands;

import de.megacord.utils.HistoryManager;
import de.megacord.utils.MySQLConnection;
import de.megacord.utils.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class KickCommand extends Command {

    public KickCommand() {
        super("kick", "megacord.command.kick");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            if (args.length < 1) {
                sender.sendMessage(new TextComponent("§cDu musst einen Spieler angeben."));
                return;
            }

            String playerName = args[0];

            ProxiedPlayer player = ProxyServer.getInstance().getPlayer((args[0]));
            if (player == null) {
                sender.sendMessage(new TextComponent("§cSpieler wurde nicht gefunden."));
                return;
            }

            String grund = args.length > 1 ? args[1] : "Es wurde kein Grund angegeben.";

            player.disconnect(new TextComponent("§8-------------------------------\n\n §3§lMegaCraft §e§lNetzwerk§r\n\n§4Du wurdest §4§lgekickt§!§r\n\n§bGrund §8» §c" + grund + "\n\n§8-------------------------------"));

                HistoryManager historyManager = new HistoryManager();
                historyManager.insertInDB(player.getUniqueId(), UUIDFetcher.getUUID(sender.getName()), "kick", grund, Long.parseLong("0"), Long.parseLong("0"), 0, 0);
                    for (ProxiedPlayer team : ProxyServer.getInstance().getPlayers()) {
                        if (team.hasPermission("megacord.punish.notify")) {
                            team.sendMessage(new TextComponent(ChatColor.GRAY + "Der " + ChatColor.GOLD + "Spieler " + ChatColor.DARK_RED + playerName + " §7wurde für §c" + grund + " §4gekicktt!"));
                        }
                    }
                }
        }
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> playerNames = new ArrayList<>();
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                playerNames.add(player.getName());
            }
            return playerNames;
        }
        return Collections.emptyList();
    }
}
