package de.megacord.commands;

import de.megacord.MegaCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class PingCommand extends Command {

    public PingCommand() {
        super("Ping");
    }

    public void execute(CommandSender sender, String[] args) {
        ProxiedPlayer p = (ProxiedPlayer)sender;
        if (args.length == 1) {
            ProxiedPlayer p2 = ProxyServer.getInstance().getPlayer(args[0]);
            p.sendMessage(MegaCord.Prefix + "§6Der §2Ping §6von " + p2 + " §7ist: §b" + p2.getPing() + "§7ms.");
            if (ProxyServer.getInstance().getPlayer(args[0]) == null) {
                p.sendMessage(MegaCord.Prefix + "§4Der Spieler ist nicht online!");
                return;
            }
        } else {
           p.sendMessage(MegaCord.Prefix + "§6Dein §bPing §7ist §b" + p.getPing() + "§7ms.");
        }
    }

}
