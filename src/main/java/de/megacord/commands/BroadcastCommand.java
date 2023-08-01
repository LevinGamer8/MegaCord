package de.megacord.commands;

import de.megacord.MegaCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class BroadcastCommand extends Command {

    public BroadcastCommand() {
        super("broadcast", "megacord.command.broadcast");
    }

    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(MegaCord.Prefix + "§4Nutze: §b/broadcast [Nachricht]");
            return;
        }
        String message = ChatColor.translateAlternateColorCodes('&', String.join(" ", args));
       ProxyServer.getInstance().broadcast(new TextComponent("§3MegaCraft§7: §b" + message));
    }
}
