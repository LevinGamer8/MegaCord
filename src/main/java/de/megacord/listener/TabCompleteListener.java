package de.megacord.listener;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class TabCompleteListener implements Listener {

    public TabCompleteListener(Plugin plugin) {
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onTabComplete(final TabCompleteEvent e) {
        final String cursor = e.getCursor().toLowerCase();
        if (!cursor.startsWith("/ban ") &&
                !cursor.startsWith("/unban ") &&
                !cursor.startsWith("/accounts ") &&
                !cursor.startsWith("/alts ") &&
                !cursor.startsWith("/alt ") &&
                !cursor.startsWith("/warns ") &&
                !cursor.startsWith("/bans ") &&
                !cursor.startsWith("/banlist ") &&
                !cursor.startsWith("/report ") &&
                !cursor.startsWith("/reports ") &&
                !cursor.startsWith("/ip ") &&
                !cursor.startsWith("/firtip ") &&
                !cursor.startsWith("/lastip ") &&
                !cursor.startsWith("/check ") &&
                !cursor.startsWith("/history ") &&
                !cursor.startsWith("/kick ") &&
                !cursor.startsWith("/warn ") &&
                !cursor.startsWith("/banadd ") &&
                !cursor.startsWith("/banremove ") &&
                !cursor.startsWith("/banidedit ") &&
                !cursor.startsWith("/onlinezeit ") &&
                !cursor.startsWith("/setmaxip ") &&
                !cursor.startsWith("/banedit ")) {
            return;
        }

        ProxiedPlayer pp = (ProxiedPlayer) e.getSender();
        if (pp.hasPermission("megacord.tabcomplete") || pp.hasPermission("megacord.*")) {
            final String[] split = cursor.split(" ");
            final String partialPlayerName = split[split.length - 1];
            for (final ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
                if (p.getName().toLowerCase().startsWith(partialPlayerName)) {
                    e.getSuggestions().add(p.getName());
                }
            }
        } else {
            e.setCancelled(true);
        }

    }

}
