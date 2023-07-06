package de.megacord.commands;

import de.megacord.utils.PlayerData;
import de.megacord.utils.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.sql.Connection;
import java.util.UUID;

public class UUIDCommand extends Command {


    public UUIDCommand() {
        super("uuid", "megacord.command.uuid");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Verwendung: /uuid <Spielername>");
            return;
        }

        UUID uuid = null;
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[0]);
        if (player != null) {
            uuid = player.getUniqueId();
        } else {
            uuid = UUIDFetcher.getUUID(args[0]);
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "Spieler nicht gefunden.");
                return;
            }
        }

        sender.sendMessage(ChatColor.GREEN + args[0] + "'s UUID: " + uuid.toString());
    }
}

