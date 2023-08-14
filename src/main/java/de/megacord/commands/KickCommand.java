package de.megacord.commands;

import de.megacord.MegaCord;
import de.megacord.utils.HistoryManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

import java.util.*;

public class KickCommand extends Command {

    private final Configuration settings;

    public KickCommand(Configuration settings) {
        super("kick", "megacord.command.kick");
        this.settings = settings;
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
            player.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', settings.getString("Kick.Disconnectmessage").replace("%reason%", grund).replace("%absatz%", "\n\n"))));

                HistoryManager historyManager = new HistoryManager();
                historyManager.insertInDB(playerName, sender.getName(), "kick", grund, Long.parseLong("0"), Long.parseLong("0"), 0, 0);

            String message = (MegaCord.Prefix + settings.getString("Kick.Kickinfo").replace("%player%", sender.getName()).replace("%target%", playerName).replace("%reason%", grund)).replace("&", "§");
            TextComponent tc = new TextComponent();
            tc.setText(message + " ");
            TextComponent tc2 = new TextComponent();
            tc2.setText(MegaCord.other2 + "[" + MegaCord.fehler + "MEHR" + MegaCord.other2 + "]");

            ArrayList<String> hoverArray = new ArrayList<>();

            int i = 1;
            while (true) {
                try {
                    String line = ChatColor.translateAlternateColorCodes('&', settings.getString("Kick.Extrainfohover." + i)).replace("%target%", playerName).replace("%name%", sender.getName()).replace("%reason%", grund).replace("%erstellt%", MegaCord.formatTime(System.currentTimeMillis()));
                    hoverArray.add(line);
                    if (i > 4) {
                        break;
                    }
                    i++;
                } catch (Exception e1) {
                    break;
                }
            }

            tc2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(String.join("\n", hoverArray))));
            tc.addExtra(tc2);

            // NACHRICHT AN ALLE ANDEREN
            for (ProxiedPlayer team : ProxyServer.getInstance().getPlayers()) {
                if ((team.hasPermission("megacord.punish.notify") || team.hasPermission("megacord.*")) || team.getName().equalsIgnoreCase(sender.getName()))
                    team.sendMessage(tc);
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
