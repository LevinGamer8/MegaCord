package de.megacord.commands;

import de.megacord.MegaCord;
import de.megacord.utils.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WarnsCommand extends Command {

    public WarnsCommand() {
        super("warns");
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            final ProxiedPlayer pp = (ProxiedPlayer) sender;
            if (pp.hasPermission("megacord.punish.warns") || pp.hasPermission("megacord.*")) {
                if (args.length == 2 || args.length == 1) { // /warns <Spieler> <Seite>
                    int seite = 1;
                    try {
                        seite = Integer.parseInt(args[1]);
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
                    }
                    sendHelp(seite, pp, args[0]);
                } //else
                    //Help message
            } else
                pp.sendMessage(new TextComponent(MegaCord.noPerm));
        } else
            sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Du bist kein Spieler!"));
    }

    private void sendHelp(int seite, ProxiedPlayer pp, String targetName) {

        UUID uuid = UUID.fromString(UUIDFetcher.getUUID(targetName));
        int zeilen = 10;
        DBUtil.getWhatCount(MegaCord.getInstance().getDataSource(), targetName, "warn", true).whenComplete((allwarns, ex) -> {
            int allPages = allwarns / zeilen;
            int eineSeitePlus = seite + 1;
            int eineSeiteMinus = seite - 1;
            if (allwarns % zeilen > 0) {
                allPages++;
            }

            if (seite > allPages || seite == 0) {
                pp.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Hier gibt es keine Warnungen!"));
                return;
            }
            pp.sendMessage(new TextComponent(" "));
            pp.sendMessage(new TextComponent(MegaCord.normal + "Warnungen von " + MegaCord.herH + targetName + " " + MegaCord.other2 + "(" + MegaCord.herH + seite + MegaCord.other2 + "/" + MegaCord.herH + allPages + MegaCord.other2 + ")"));

            HistoryManager historyManager = new HistoryManager();
            List<HistoryElement> warns = historyManager.readHistory(targetName, zeilen, seite, "warn", false);
            for (HistoryElement warn : warns) {
                TextComponent tc = new TextComponent();
                tc.setText(MegaCord.Prefix);

                TextComponent tc1 = new TextComponent();
                tc1.setText(MegaCord.normal + ChatColor.translateAlternateColorCodes('&', Config.settings.getString("Warns.Message").replace("%grund%", warn.getGrund()).replace("%wer%", warn.getTargetName()).replace("%von%", warn.getVonName()).replace("%time%", MegaCord.formatTime(warn.getErstellt()))));
                tc.addExtra(tc1);

                TextComponent tc2 = new TextComponent();
                tc2.setText(MegaCord.other2 + "[" + MegaCord.fehler + "MEHR" + MegaCord.other2 + "]");
                ArrayList<String> hoverArray = new ArrayList<>();
                int i = 1;
                while (true) {
                    try {
                        String line = ChatColor.translateAlternateColorCodes('&', Config.settings.getString("Warns.hover." + i).replace("%grund%", warn.getGrund()).replace("%wer%", warn.getTargetName()).replace("%von%", warn.getVonName()).replace("%time%", MegaCord.formatTime(warn.getErstellt())));
                        hoverArray.add(line);
                        i++;
                        if (i > 3)
                            break;
                    } catch (Exception e1) {
                        break;
                    }
                }
                tc2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(String.join("\n", hoverArray))));
                tc.addExtra(tc2);

                TextComponent tc3 = new TextComponent();
                tc3.setText(MegaCord.other2 + " [" + MegaCord.fehler + "LÖSCHEN" + MegaCord.other2 + "]");
                tc3.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warn del " + warn.getErstellt()));
                tc3.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text((MegaCord.other2 + "Lösche diese Warnung"))));
                tc.addExtra(tc3);

                pp.sendMessage(tc);
                tc.getExtra().clear();
            }

            pfeile(eineSeiteMinus, eineSeitePlus, pp, targetName);
        });
    }

    private void pfeile(int eineSeiteMinus, int eineSeitePlus, ProxiedPlayer pp, String targetName) {
        TextComponent tc = new TextComponent();
        tc.setText(MegaCord.Prefix);

        TextComponent tc1 = new TextComponent();
        tc1.setText("§f«« ");
        tc1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warns " + targetName + " " + eineSeiteMinus));
        tc1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§8(§7Seite: " + MegaCord.herH + eineSeiteMinus + "§8)")));
        tc.addExtra(tc1);

        TextComponent tc2 = new TextComponent();
        tc2.setText(MegaCord.other2 + "[" + MegaCord.fehler + "KLICK" + MegaCord.other2 + "]");
        tc2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MegaCord.fehler + "Klick auf die Pfeile!")));
        tc.addExtra(tc2);

        TextComponent tc3 = new TextComponent();
        tc3.setText(" §f»»");
        tc3.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warns " + targetName + " " + eineSeitePlus));
        tc3.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§8(§7Seite: " + MegaCord.herH + eineSeitePlus + "§8)")));
        tc.addExtra(tc3);

        pp.sendMessage(tc);
    }

}