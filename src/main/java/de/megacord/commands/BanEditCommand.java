package de.megacord.commands;

import de.megacord.MegaCord;
import de.megacord.utils.BanUtils;
import de.megacord.utils.Config;
import de.megacord.utils.UUIDFetcher;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BanEditCommand extends Command {

    public BanEditCommand() {
        super("banedit", "megacord.punish.banedit");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender.hasPermission("megacord.punish.ban.edit") || sender.hasPermission("megacord.*")) {
            // editban <name> <type> <value>
            if (args.length == 1) {
                sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Benutze folgende Types:\n" + MegaCord.herH + "1 §f» " + MegaCord.normal + "Änder, ob es ein Ban oder Mute ist\n" + MegaCord.herH + "2 §f» " + MegaCord.normal + "Änder bis wann der Ban geht\n" + MegaCord.herH + "3 §f» " + MegaCord.normal + "Änder den Grund"));
                return;
            }
            if (args.length >= 2) {
                ProxiedPlayer p = ProxyServer.getInstance().getPlayer(args[0]);
                BanUtils ban = new BanUtils(p.getName(), null, MegaCord.getInstance().getDataSource(), Config.settings, Config.standardBans);
                final boolean[] retrun = new boolean[1];
                ban.isBanned(p.getName()).whenComplete((result, ex) -> {
                    if(!result) {
                        sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Der Spieler " + MegaCord.herH + args[0] + MegaCord.fehler + " ist nicht gebannt!"));
                        retrun[0] = true;
                    }
                });
                if(retrun[0])
                    return;
                ProxiedPlayer pp = null;
                if (sender instanceof ProxiedPlayer)
                    pp = (ProxiedPlayer) sender;
                if (args[1].equalsIgnoreCase("1")) { // Ban (Ban/Mute)
                    if (args.length == 3) {
                        int value;
                        try {
                            value = Integer.parseInt(args[2]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Der Wert muss eine Zahl sein!"));
                            return;
                        }
                        if (value == 1 || value == 0) {
                            if(ban.getBan() == value) {
                                sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Der Ban existiert bereits in der Form!"));
                                return;
                            }
                            ban.editban(1, args[2], pp == null ? UUIDFetcher.getUUID("CONSOLE").toString() : pp.getUniqueId().toString());
                        } else {
                            sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Der Wert muss eine " + MegaCord.herH + "1 " + MegaCord.fehler + "oder eine " + MegaCord.herH + "0 " + MegaCord.fehler + "sein!"));
                            return;
                        }
                    } else {
                        sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Gib noch einen neuen Wert (Value) ein"));                        return;
                    }
                } else if (args[1].equalsIgnoreCase("2")) { // Bis
                    if (args.length == 3) {
                        final String regex = "^(0\\d|1\\d|2\\d|3[0-1])\\/(0[1-9]|1[0-2])\\/(20\\d\\d)T(0\\d|1\\d|2[0-3])\\:([0-5]\\d)";

                        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
                        final Matcher matcher = pattern.matcher(args[2]);

                        while (matcher.find() || args[2].equalsIgnoreCase("-1")) {
                            ban.editban(2, args[2], pp == null ? UUIDFetcher.getUUID("CONSOLE").toString() : pp.getUniqueId().toString());
                            sender.sendMessage(new TextComponent(MegaCord.Prefix + "Der Ban wurde verändert!"));
                            return;
                        }
                        if (!matcher.find()) {
                            TextComponent tc = new TextComponent();
                            tc.setText(MegaCord.Prefix + MegaCord.fehler + "Das Datum konnte nicht gefunden werden!");
                            TextComponent tc1 = new TextComponent(MegaCord.other2 + " [" + MegaCord.fehler + "MEHR" + MegaCord.other2 + "]");
                            tc1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MegaCord.normal + "Stelle sicher, dass du es im richtigen Format geschrieben hast!\n" + MegaCord.normal + "Richtig: " + MegaCord.herH + "DD/MM/YYYYThh:mm " + MegaCord.other2 + "(" + MegaCord.normal + "Beispiel: " + MegaCord.herH + "10/04/2020T14:56" + MegaCord.other2 + ")\n" + MegaCord.normal + "Falsch: " + MegaCord.herH + args[2])));
                            tc.addExtra(tc1);
                            sender.sendMessage(tc);
                            return;
                        }
                    } else {
                        sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Gib noch einen neuen Wert (Value) ein"));
                        return;
                    }
                } else if (args[1].equalsIgnoreCase("3")) { // Grund
                    if (args.length >= 3) {
                        String newReason = "";
                        for (int i = 2; i < args.length; i++) {
                            newReason = newReason + args[i] + " ";
                        }
                        if(newReason.equalsIgnoreCase(ban.getGrund())) {
                            sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Der Ban existiert bereits in der Form!"));
                            return;
                        }
                        ban.editban(3, newReason, pp == null ? UUIDFetcher.getUUID("CONSOLE").toString() : pp.getUniqueId().toString());
                    } else {
                        sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Gib noch einen neuen Wert (Value) ein"));
                        return;
                    }
                } else {
                    sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Benutze folgende Types:\n" + MegaCord.herH + "1 §f» " + MegaCord.normal + "Änder, ob es ein Ban oder Mute ist\n" + MegaCord.herH + "2 §f» " + MegaCord.normal + "Änder bis wann der Ban geht\n" + MegaCord.herH + "3 §f» " + MegaCord.normal + "Änder den Grund"));
                    return;
                }
                sender.sendMessage(new TextComponent(MegaCord.Prefix + "Der Ban wurde verändert!"));
            } else {
                sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + ""));
            }
        } else
            sender.sendMessage(new TextComponent(MegaCord.noPerm));
    }

}
