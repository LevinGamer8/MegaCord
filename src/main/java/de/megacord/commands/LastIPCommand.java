package de.megacord.commands;

import java.util.UUID;

import de.megacord.MegaCord;
import de.megacord.utils.PlayerData;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;


public class LastIPCommand extends Command {


    public LastIPCommand() {
        super("lastip", "megacord.command.ip", "ip");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(new ComponentBuilder("§cVerwendung: /ip <spielername>").create());
            return;
        }

        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);

            if(target == null) {
                sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Dieser Spieler existiert nicht!"));
                return;
            }
            String ip = new PlayerData(target.getName()).getLastip();
            if (ip == null || ip.equals("")) {
                sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.herH + args[0] + MegaCord.fehler + " war noch nie auf dem Netzwerk!"));
                return;
            }
            sender.sendMessage(new TextComponent(MegaCord.Prefix + "Die IP von "+ MegaCord.herH + args[0] + MegaCord.normal+" ist: "+ MegaCord.herH + ip));



        }
    }
}