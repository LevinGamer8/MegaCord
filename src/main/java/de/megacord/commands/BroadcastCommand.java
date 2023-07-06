package de.megacord.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;

public class BroadcastCommand extends Command {

    public BroadcastCommand() {
        super("broadcast", "megacord.command.broadcast");
    }

    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Verwendung: /broadcast <Nachricht>");
            return;
        }

        String message = ChatColor.translateAlternateColorCodes('&', String.join(" ", args));

        // Sende die Nachricht an alle Server, die online sind
        for (String server : ProxyServer.getInstance().getServers().keySet()) {
            ProxyServer.getInstance().getServerInfo(server).sendData("Forward", message.getBytes());
        }

        sender.sendMessage(ChatColor.GREEN + "Die Nachricht wurde an alle Server gesendet");
    }
}
