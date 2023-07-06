package de.megacord.listener;

import de.megacord.MegaCord;
import de.megacord.commands.BanAddCommand;
import de.megacord.utils.DateUnit;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class BanAddListener implements Listener {

    public BanAddListener(Plugin plugin) {
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }


    @EventHandler
    public void onChat(ChatEvent e){
        if (e.getMessage().startsWith("/")) {
            return;
        }
        if (!e.getSender().equals(BanAddCommand.p)) return;
        if (BanAddCommand.finished) return;
        e.setCancelled(true);
        try {
            switch (BanAddCommand.phase){
                case 1:
                    if (e.getMessage().equalsIgnoreCase("1") || e.getMessage().equalsIgnoreCase("0")) {
                        BanAddCommand.perma = Integer.parseInt(e.getMessage());
                        BanAddCommand.phase++;
                        BanAddCommand.startPhase(BanAddCommand.phase);
                    } else {
                        BanAddCommand.p.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Nutze: " + MegaCord.herH + "1 " + MegaCord.fehler + "oder " + MegaCord.herH + "0"));
                    }
                    break;
                case 2:
                    if (e.getMessage().equalsIgnoreCase("1") || e.getMessage().equalsIgnoreCase("0")) {
                        BanAddCommand.ban = Integer.parseInt(e.getMessage());
                        BanAddCommand.phase++;
                        BanAddCommand.startPhase(BanAddCommand.phase);
                    } else {
                        BanAddCommand.p.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Nutze: " + MegaCord.herH + "1 " + MegaCord.fehler + "oder " + MegaCord.herH + "0"));
                    }
                    break;
                case 3:
                    BanAddCommand.grund = e.getMessage();
                    BanAddCommand.phase++;
                    BanAddCommand.startPhase(BanAddCommand.phase);
                    break;
                case 4:
                    try {
                        BanAddCommand.format = e.getMessage();
                        DateUnit.valueOf((BanAddCommand.format.toUpperCase()));
                    } catch (IllegalArgumentException ex) {
                        BanAddCommand.p.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.herH + BanAddCommand.format + MegaCord.fehler + " ist keine gültige Einheit"));
                        BanAddCommand.p.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.normal + "Gültige Einheiten: "));
                        for (DateUnit date : DateUnit.values()) {
                            BanAddCommand.p.sendMessage(new TextComponent(MegaCord.herH + date));
                        }
                        return;
                    }
                    BanAddCommand.phase++;
                    BanAddCommand.startPhase(BanAddCommand.phase);
                    break;
                case 5:
                    BanAddCommand.dauer = Integer.parseInt(e.getMessage());
                    if (BanAddCommand.dauer > 0) {
                        BanAddCommand.phase++;
                        BanAddCommand.startPhase(BanAddCommand.phase);
                    } else
                        BanAddCommand.p.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Nutze eine Zahl, die größer als 0 ist!"));
                    break;
                case 6:
                    if (e.getMessage().equalsIgnoreCase("1") || e.getMessage().equalsIgnoreCase("0")) {
                        BanAddCommand.report = Integer.parseInt(e.getMessage());
                        BanAddCommand.phase++;
                        BanAddCommand.startPhase(BanAddCommand.phase);
                    } else
                        BanAddCommand.p.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Nutze: " + MegaCord.herH + "1 " + MegaCord.fehler + "oder " + MegaCord.herH + "0"));
                    break;
                case 7:
                    if (e.getMessage().equalsIgnoreCase("0")) {
                        BanAddCommand.finishSetup();
                        return;
                    }
                    BanAddCommand.perm = e.getMessage();
                    BanAddCommand.finishSetup();
                    break;
            }
        } catch (NumberFormatException ex) {
            BanAddCommand.p.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Gebe eine zahl ein!"));
        }

    }

}
