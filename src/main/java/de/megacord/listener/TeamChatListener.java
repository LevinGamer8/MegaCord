package de.megacord.listener;

import de.megacord.MegaCord;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.ProxyServer;

public class TeamChatListener implements Listener {
    @EventHandler
    public void onTeamChat(ChatEvent e) {
        ProxiedPlayer p = (ProxiedPlayer) e.getSender();
        if (p.hasPermission("megacord.teamchat")) {
            String[] str = e.getMessage().split(" ");
            if (str.length > 1 && str[0].equalsIgnoreCase("@team")) {
                StringBuilder msgBuilder = new StringBuilder();
                msgBuilder.append("§7[§3Team§2Chat§7] §b")
                        .append(p.getName())
                        .append(" §5von §a")
                        .append(p.getServer().getInfo().getName())
                        .append(" §7:§b ");
                for (int i = 1; i < str.length; ++i) {
                    msgBuilder.append("§7").append(str[i]).append(" ");
                }
                String msg = msgBuilder.toString();
                for (ProxiedPlayer team : ProxyServer.getInstance().getPlayers()) {
                    if (team.hasPermission("megacord.teamchat")) {
                        team.sendMessage(new TextComponent(msg));
                    }
                }
                e.setCancelled(true);
            } else if (str[0].equalsIgnoreCase("@team")) {
                p.sendMessage(new TextComponent(MegaCord.Prefix + "§4Du musst eine Nachricht eingeben."));
                e.setCancelled(true);
            }
        }
    }
}