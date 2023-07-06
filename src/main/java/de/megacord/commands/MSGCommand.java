package de.megacord.commands;

import de.megacord.MegaCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class MSGCommand extends Command {

    public MSGCommand() {
        super("msg");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            if (args.length == 0) {
                sender.sendMessage(new TextComponent(ChatColor.RED + "Nutze: /msg <Spieler> <Nachricht>"));
            } else {
                ProxiedPlayer p = ProxyServer.getInstance().getPlayer(args[0]);
                if (p == null) {
                    sender.sendMessage(new TextComponent(MegaCord.Prefix + "§6Spieler " + args[0] + " §4wurde nicht gefunden§7!"));
                } else {
                    StringBuilder msgBuilder = new StringBuilder();

                    for(int i = 1; i < args.length; ++i) {
                        msgBuilder.append(args[i]).append(" ");
                    }

                    String msg = ChatColor.translateAlternateColorCodes('&', msgBuilder.toString().trim());
                    p.sendMessage(new TextComponent("§7[§3MSG§7] §7[§a" + sender.getName() + "§7] §b-> §7[§6DIR§7] §b-> §7" + msg));
                    sender.sendMessage(new TextComponent("§7[§3MSG§7] §7[§6DU§7] §b-> §7[§a" + p.getName() + "§7] §b-> §7" + msg));
                }
            }
        } else {
            sender.sendMessage(new TextComponent(ChatColor.RED + "You must be a player!"));
        }

    }
}
