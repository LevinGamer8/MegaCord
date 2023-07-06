package de.megacord.commands;

import de.megacord.MegaCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ChatClearCommand extends Command{

        public ChatClearCommand() {
            super("chatclear", "megacord.command.chatclear", new String[]{"cc"});
        }

        public void execute(CommandSender sender, String[] args) {
            if (sender instanceof ProxiedPlayer) {
                ProxiedPlayer p = (ProxiedPlayer)sender;
                if (p.hasPermission("megacord.command.chatclear")) {
                    p.sendMessage(MegaCord.Prefix + "§4Du hast keine Rechte!");
                }

                for(int i = 0; i < 1000; ++i) {
                    ProxyServer.getInstance().broadcast(new TextComponent(""));
                }

                ProxyServer.getInstance().broadcast(new TextComponent(MegaCord.Prefix + "§eDer Chat §7wurde §bgecleart§7."));
            } else {
                sender.sendMessage(MegaCord.Prefix + "§4Dieser Command geht nur als Spieler");
            }

        }
    }
