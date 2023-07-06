package de.megacord.commands;

import de.megacord.MegaCord;
import de.megacord.utils.Onlinezeit;
import de.megacord.utils.UUIDFetcher;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OnlineZeitCommand extends Command {


        public OnlineZeitCommand() {
            super("onlinezeit", "", "oz", "onlinetime", "ot");
        }

        // onlinezeit -> zeigt alle, die Online waren am heutigen Tage an (0) (total)

        // onlinezeit week -> Zeigt dir deine Woche an, also wann du Online warst (1)
        // onlinezeit trend -> Zeigt einen Onlinezeit Trend von dir für die letzten 7 Tage(1)
        // onlinezeit total -> Zeigt deine gesamte Onlinezeit (1)
        // onlinezeit <Datum> -> zeigt alle, die Online waren, am Datum, an (1)
        // onlinezeit <Spieler> -> zeigt an, wie lange der angegebende User am heutigen Tage online war (1) (total)

        // onlinezeit total top -> zeigt die 3 "besten" Spieler von der Onlinezeit her an. Für den gesamten Zeitraum (alles zsm gerechnet) (2)
        // onlinezeit week top -> zeigt die 3 "besten" Spieler von der Onlinezeit her an. Für die letzte Woche (alles zsm gerechnet) (2)
        // onlinezeit week <Spieler> -> Zeigt dir die Woche eines Spielers an, also wann er Online war (2)
        // onlinezeit trend <Tage> -> Zeigt einen Onlinezeit Trend von dir der letzten angegebenen Tage (2)
        // onlinezeit trend <Spieler> -> Zeigt einen Onlinezeit Trend von der Person (2)
        // onlinezeit total <Spieler> -> Zeigt die gesamte Onlinezeit eines Spielers (2)
        // onlinezeit <Spieler> <Datum> -> zeigt an, wie lange der angegebende User am Datum online war (2)

        // onlinezeit trend <Spieler> <Tage> -> Zeigt einen Onlinezeit Trend von der Person mit der angegebenen Tagezahl (3)
        @Override
        public void execute(CommandSender sender, String[] args) {
            Onlinezeit onlinezeit = new Onlinezeit(sender, MegaCord.getInstance().getDataSource());
            if (args.length == 0) { // /onlinezeit
                if (sender.hasPermission("megacord.onlinezeit.other") || sender.hasPermission("megacord.*") || sender.hasPermission("megacord.onlinezeit.*")) { // /onlinezeit
                    onlinezeit.sendFromDate(System.currentTimeMillis(), null);
                    return;
                } else if (sender.hasPermission("megacord.onlinezeit.own") || sender.hasPermission("megacord.*") || sender.hasPermission("megacord.onlinezeit.*")) { // /onlinezeit
                    if (sender instanceof ProxiedPlayer)
                        onlinezeit.sendFromDate(System.currentTimeMillis(), sender.getName());
                    else
                        sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Du bist kein Spieler!"));
                    return;
                } else
                    sender.sendMessage(new TextComponent(MegaCord.noPerm));
            } else if (args.length == 1) {
                if (args[0].contains("/")) { // /onlinezeit 10/06/2020
                    if (sender.hasPermission("megacord.onlinezeit.datum") || sender.hasPermission("megacord.*") || sender.hasPermission("megacord.onlinezeit.*")) { // /onlinezeit <Datum>
                        if (checkString(args[0])) {
                            // COMMAND START
                            String[] stringSplit = args[0].split("/");
                            onlinezeit.sendFromDate(Timestamp.from(LocalDate.of(Integer.parseInt(stringSplit[2]), Integer.parseInt(stringSplit[1]), Integer.parseInt(stringSplit[0])).atStartOfDay(ZoneId.of("Europe/Berlin")).toInstant()).getTime(), null);
                        } else {
                            TextComponent tc = new TextComponent();
                            tc.setText(MegaCord.Prefix + MegaCord.fehler + "Das Datum konnte nicht gefunden werden! ");
                            TextComponent tc1 = new TextComponent(MegaCord.other2 + "[" + MegaCord.fehler + "MEHR" + MegaCord.other2 + "]");
                            tc1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MegaCord.normal + "Stelle sicher, dass du es im richtigen Format geschrieben hast!\n" + MegaCord.normal + "Richtig: " + MegaCord.herH + "DD/MM/YYYY " + MegaCord.other2 + "(" + MegaCord.normal + "Beispiel: " + MegaCord.herH + "10/04/2020" + MegaCord.other2 + ")\n" + MegaCord.normal + "Falsch: " + MegaCord.herH + args[0])));
                            tc.addExtra(tc1);
                            sender.sendMessage(tc);
                        }
                        return;
                    } else
                        sender.sendMessage(new TextComponent(MegaCord.noPerm));
                } else {
                    if (args[0].equalsIgnoreCase("total")) { // /onlinezeit total
                        if (sender.hasPermission("megacord.onlinezeit.total") || sender.hasPermission("megacord.*") || sender.hasPermission("megacord.onlinezeit.*")) {
                            if (sender instanceof ProxiedPlayer)
                                onlinezeit.sendFromDate(null, sender.getName());
                            else
                                sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Du bist doch eh die ganze Zeit online :)"));
                        } else
                            sender.sendMessage(new TextComponent(MegaCord.noPerm));
                        return;
                    } else if (args[0].equalsIgnoreCase("trend")) {// /onlinezeit trend
                        if (sender.hasPermission("megacord.onlinezeit.trend.own") || sender.hasPermission("megacord.*") || sender.hasPermission("megacord.onlinezeit.*")) {
                            if (sender instanceof ProxiedPlayer) {
                                onlinezeit.sendTrend(UUIDFetcher.getUUID(sender.getName()), 7, false);
                            } else
                                sender.sendMessage(new TextComponent(MegaCord.Prefix + "Du bist doch eh die ganze Zeit online :)"));
                        } else
                            sender.sendMessage(new TextComponent(MegaCord.noPerm));
                        return;
                    } else if (args[0].equalsIgnoreCase("week")) { // /onlinezeit week
                        if (sender.hasPermission("megacord.onlinezeit.week.own") || sender.hasPermission("megacord.*") || sender.hasPermission("megacord.onlinezeit.*")) {
                            if (sender instanceof ProxiedPlayer) {
                                onlinezeit.sendWeek(((ProxiedPlayer) sender).getUniqueId().toString(), false);
                                return;
                            } else
                                sender.sendMessage(new TextComponent(MegaCord.Prefix + "Du bist doch eh die ganze Zeit online :)"));
                        } else
                            sender.sendMessage(new TextComponent(MegaCord.noPerm));
                        return;
                    }
                    if (sender.hasPermission("megacord.onlinezeit.player") || sender.hasPermission("megacord.*") || sender.hasPermission("megacord.onlinezeit.*")) {
                        // /onlinezeit <Spieler>
                        if (UUIDFetcher.getUUID(args[0]) == null) {
                            sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Dieser Spieler existiert nicht!"));
                            return;
                        }
                        onlinezeit.sendFromDate(System.currentTimeMillis(), args[0]);
                    } else
                        sender.sendMessage(new TextComponent(MegaCord.noPerm));
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("total")) { // /onlinezeit total <Spieler>
                    if(args[1].equalsIgnoreCase("top")) {
                        if (sender.hasPermission("megacord.onlinezeit.total.top") || sender.hasPermission("megacord.*") || sender.hasPermission("megacord.onlinezeit.*")) {
                            onlinezeit.sendTop(false, !(sender instanceof ProxiedPlayer));
                            return;
                        } else
                            sender.sendMessage(new TextComponent(MegaCord.noPerm));
                        return;
                    }
                    if (sender.hasPermission("megacord.onlinezeit.total.other") || sender.hasPermission("megacord.*") || sender.hasPermission("megacord.onlinezeit.*")) {
                        if (UUIDFetcher.getUUID(args[1]) == null) {
                            sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Dieser Spieler existiert nicht!"));
                            return;
                        }
                        onlinezeit.sendFromDate(null, args[1]);
                        return;
                    } else
                        sender.sendMessage(new TextComponent(MegaCord.noPerm));
                    return;
                } else if (args[0].equalsIgnoreCase("trend")) { // /onlinezeit trend <Tage> ODER /onlinezeit trend <Spieler>
                    try { // /onlinezeit trend <Tage>
                        if (sender.hasPermission("megacord.onlinezeit.trend.own.days") || sender.hasPermission("megacord.*") || sender.hasPermission("megacord.onlinezeit.*")) {
                            onlinezeit.sendTrend(UUIDFetcher.getUUID(sender.getName()), Integer.parseInt(args[1]), !(sender instanceof ProxiedPlayer));
                        }
                        return;
                    } catch (NumberFormatException e) { // /onlinezeit trend <Spieler>
                        if (sender.hasPermission("megacord.onlinezeit.trend.other") || sender.hasPermission("megacord.*") || sender.hasPermission("megacord.onlinezeit.*")) {
                            if (UUIDFetcher.getUUID(args[1]) == null) {
                                sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Dieser Spieler existiert nicht!"));
                                return;
                            }
                            onlinezeit.sendTrend(UUIDFetcher.getUUID(args[1]), 7, !(sender instanceof ProxiedPlayer));
                        }
                        return;
                    }
                } else if (args[0].equalsIgnoreCase("week")) { // /onlinezeit week <Spieler>
                    if(args[1].equalsIgnoreCase("top")) {
                        if (sender.hasPermission("megacord.onlinezeit.week.top") || sender.hasPermission("megacord.*") || sender.hasPermission("megacord.onlinezeit.*")) {
                            onlinezeit.sendTop(true, !(sender instanceof ProxiedPlayer));
                            return;
                        } else
                            sender.sendMessage(new TextComponent(MegaCord.noPerm));
                        return;
                    }
                    if (sender.hasPermission("megacord.onlinezeit.week.other") || sender.hasPermission("megacord.*") || sender.hasPermission("megacord.onlinezeit.*")) {
                        if (UUIDFetcher.getUUID(args[1]) == null) {
                            sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Dieser Spieler existiert nicht!"));
                            return;
                        }
                        onlinezeit.sendWeek(UUIDFetcher.getUUID(args[1]).toString(), !(sender instanceof ProxiedPlayer));
                        return;
                    }
                    return;
                }
                if (sender.hasPermission("megacord.onlinezeit.player.datum") || sender.hasPermission("megacord.*") || sender.hasPermission("megacord.onlinezeit.*")) {
                    // /onlinezeit <Spieler> <Datum>
                    if (UUIDFetcher.getUUID(args[0]) == null) {
                        sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Dieser Spieler existiert nicht!"));
                        return;
                    }
                    if (checkString(args[1])) {
                        String[] stringSplit = args[1].split("/");
                        onlinezeit.sendFromDate(Timestamp.from(LocalDate.of(Integer.parseInt(stringSplit[2]), Integer.parseInt(stringSplit[1]), Integer.parseInt(stringSplit[0])).atStartOfDay(ZoneId.of("Europe/Berlin")).toInstant()).getTime(), args[0]);
                    } else {
                        TextComponent tc = new TextComponent();
                        tc.setText(MegaCord.Prefix + MegaCord.fehler + "Das Datum konnte nicht gefunden werden! ");
                        TextComponent tc1 = new TextComponent(MegaCord.other2 + "[" + MegaCord.fehler + "MEHR" + MegaCord.other2 + "]");
                        tc1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MegaCord.normal + "Stelle sicher, dass du es im richtigen Format geschrieben hast!\n" + MegaCord.normal + "Richtig: " + MegaCord.herH + "DD/MM/YYYY " + MegaCord.other2 + "(" + MegaCord.normal + "Beispiel: " + MegaCord.herH + "10/04/2020" + MegaCord.other2 + ")\n" + MegaCord.normal + "Falsch: " + MegaCord.herH + args[0])));
                        tc.addExtra(tc1);
                        sender.sendMessage(tc);
                    }
                } else
                    sender.sendMessage(new TextComponent(MegaCord.noPerm));
            } else if (args.length == 3) {
                // /onlinezeit trend <Spieler> <Tage>
                if (args[0].equalsIgnoreCase("trend")) {
                    if (sender.hasPermission("megacord.onlinezeit.trend.other.days") || sender.hasPermission("megacord.*") || sender.hasPermission("megacord.onlinezeit.*")) {
                        if (UUIDFetcher.getUUID(args[1]) == null) {
                            sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Dieser Spieler existiert nicht!"));
                            return;
                        }
                        try {
                            onlinezeit.sendTrend(UUIDFetcher.getUUID(args[1]), Integer.parseInt(args[2]), !(sender instanceof ProxiedPlayer));
                            return;
                        } catch (NumberFormatException e) {
                            sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Die Tage müssen als Zahl angegeben sein! (Deins: "+args[2]+")"));
                            return;
                        }
                    }
                }
            } //else
               //help message
        }

        private boolean checkString(String toCheck) {
            final String regex = "^(0\\d|1\\d|2\\d|3[0-1])\\/(0[1-9]|1[0-2])\\/(20\\d\\d)";

            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(toCheck);
            return matcher.find();
        }


}
