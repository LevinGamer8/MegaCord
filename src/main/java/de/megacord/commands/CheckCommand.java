package de.megacord.commands;

import de.megacord.MegaCord;
import de.megacord.utils.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.UUID;

public class CheckCommand extends Command {

    public CheckCommand() {
        super("Check");
    }

    private TextComponent tc;
    private TextComponent tc1;
    private boolean targetConnected;


    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender.hasPermission("megacord.punish.check") || sender.hasPermission("megacord.*")) {
            if (args.length == 1) {
                ProxiedPlayer p = ProxyServer.getInstance().getPlayer(args[0]);
                if (p == null) {
                    targetConnected = false;
                } else if (p.isConnected()) {
                    targetConnected = true;
                }
                sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.normal + "Checke " + MegaCord.herH + args[0]));

                check(p, sender);
            } //else
                //Help Message
        } else
            sender.sendMessage(new TextComponent(MegaCord.noPerm));
    }

    private void resetTC() {
        tc = new TextComponent();
        tc1 = new TextComponent();
    }

    private void check(ProxiedPlayer p, CommandSender sender) {
        BanUtils ban = new BanUtils(p.getName(), null, MegaCord.getInstance().getDataSource(), Config.settings, Config.standardBans);
        resetTC();
        if (tc.getExtra() != null)
            tc.getExtra().clear();
        ban.isBanned(p.getName()).whenComplete((result, ex) -> {
            if (result) {
                tc.setText(MegaCord.Prefix + ChatColor.translateAlternateColorCodes('&', Config.settings.getString("Check.status").replace("%status%", (ban.getBan() == 0 ? "Gemutet" : "Gebannt"))));
                String hover1 = Config.settings.getString("Check.hover.1");
                String hover2 = Config.settings.getString("Check.hover.2");
                String hover3 = Config.settings.getString("Check.hover.3");
                String hover4 = Config.settings.getString("Check.hover.4");
                if (sender instanceof ProxiedPlayer) {
                    tc1.setText(MegaCord.other2 + " [" + MegaCord.fehler + "MEHR" + MegaCord.other2 + "]");
                    if (!ban.getBeweis().equals("/"))
                        tc1.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ban.getBeweis()));
                    tc1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.translateAlternateColorCodes('&', hover1 + "\n" + hover2 + "\n" + hover3 + "\n" + hover4 + (!ban.getBeweis().equals("/") ? ("\n" + MegaCord.normal + "Klick um den " + MegaCord.herH + "Beweislink" + MegaCord.normal + " zu öffnen!") : "\n" + MegaCord.fehler + "Kein Beweislink angegeben")).replace("%name%", ban.getVonName()).replace("%reason%", ban.getGrund()).replace("%bis%", ban.getBis() == -1 ? "§3Permanent" : MegaCord.formatTime(ban.getBis())).replace("%editby%", ban.getEditBy()))));
                } else {
                    tc1.setText(ChatColor.translateAlternateColorCodes('&', "\n" + hover1 + "\n" + hover2 + "\n" + hover3 + "\n" + hover4 + "\n" + MegaCord.normal + "Beweis: " + MegaCord.herH + ban.getBeweis()).replace("%name%", ban.getVonName()).replace("%reason%", ban.getGrund()).replace("%bis%", ban.getBis() == -1 ? "§3Permanent" : MegaCord.formatTime(ban.getBis())).replace("%editby%", ban.getEditBy()));
                }
                tc.addExtra(tc1);
            } else
                tc.setText(MegaCord.Prefix + ChatColor.translateAlternateColorCodes('&', Config.settings.getString("Check.status").replace("%status%", "Nicht Gebannt/Gemuted")));
            sender.sendMessage(tc);

            resetTC();
            if (tc.getExtra() != null)
                tc.getExtra().clear();
            DBUtil.getWhatCount(MegaCord.getInstance().getDataSource(), p.getName(), "report", true).whenComplete((reports, exe) -> {

                tc.setText(MegaCord.Prefix + ChatColor.translateAlternateColorCodes('&', Config.settings.getString("Check.reports").replace("%reportCount%", (reports == -1 || reports == 0 ? "§cKeine" : String.valueOf(reports)))));
                if (reports >= 1) {
                    tc1.setText(MegaCord.other2 + " [" + MegaCord.fehler + "MEHR" + MegaCord.other2 + "]");
                    tc1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MegaCord.other2 + "(" + MegaCord.fehler + "Click" + MegaCord.other2 + ")")));
                    tc1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reports " + p.getName() + " 1"));
                    if (sender instanceof ProxiedPlayer)
                        tc.addExtra(tc1);
                }
                sender.sendMessage(tc);
                resetTC();

                if (tc.getExtra() != null)
                    tc.getExtra().clear();
                DBUtil.getWhatCount(MegaCord.getInstance().getDataSource(), p.getName(), "warn", true).whenComplete((warns, exc) -> {
                    tc.setText(MegaCord.Prefix + ChatColor.translateAlternateColorCodes('&', Config.settings.getString("Check.warns").replace("%warnsCount%", (warns == -1 || warns == 0 ? "§cKeine" : String.valueOf(warns)))));
                    if (warns != 0) {
                        tc1.setText(MegaCord.other2 + " [" + MegaCord.fehler + "MEHR" + MegaCord.other2 + "]");
                        tc1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MegaCord.other2 + "(" + MegaCord.fehler + "Click" + MegaCord.other2 + ")")));
                        tc1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warns " + p.getName()));
                        if (sender instanceof ProxiedPlayer)
                            tc.addExtra(tc1);
                    }
                    sender.sendMessage(tc);

                    resetTC();
                    if (tc.getExtra() != null)
                        tc.getExtra().clear();
                    DBUtil.getWhatCount(MegaCord.getInstance().getDataSource(), p.getName(), "ban", true).whenComplete((bans, exception) -> {
                        tc.setText(MegaCord.Prefix + ChatColor.translateAlternateColorCodes('&', Config.settings.getString("Check.bans").replace("%bansCount%", (bans == -1 || bans == 0 ? "§cKeine" : String.valueOf(bans)))));
                        if (bans != 0) {
                            tc1.setText(MegaCord.other2 + " [" + MegaCord.fehler + "MEHR" + MegaCord.other2 + "]");
                            tc1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MegaCord.other2 + "(" + MegaCord.fehler + "Click" + MegaCord.other2 + ")")));
                            tc1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banlist " + p.getName()));
                            if (sender instanceof ProxiedPlayer)
                                tc.addExtra(tc1);
                        }
                        sender.sendMessage(tc);

                        resetTC();
                        if (tc.getExtra() != null)
                            tc.getExtra().clear();
                        int historyCount = bans + warns + reports;
                        tc.setText(MegaCord.Prefix + ChatColor.translateAlternateColorCodes('&', Config.settings.getString("Check.history").replace("%historyCount%", (historyCount == -1 || historyCount == 0 ? "§cKeine" : String.valueOf(historyCount)))));
                        if (historyCount != 0) {
                            tc1.setText(MegaCord.other2 + " [" + MegaCord.fehler + "MEHR" + MegaCord.other2 + "]");
                            tc1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MegaCord.other2 + "(" + MegaCord.fehler + "Click" + MegaCord.other2 + ")")));
                            tc1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banlist " + p.getName()));
                            if (sender instanceof ProxiedPlayer)
                                tc.addExtra(tc1);
                        }
                        sender.sendMessage(tc);

                        resetTC();
                        //                        STATS
                        if (tc.getExtra() != null)
                            tc.getExtra().clear();
                        tc.setText(MegaCord.Prefix + MegaCord.normal + ChatColor.translateAlternateColorCodes('&', Config.settings.getString("Check.stats")));

                        ArrayList<String> hoverArray = new ArrayList<>();
                        int i = 1;
                        hoverArray.add(MegaCord.fehler + "Dieser Spieler war noch nie auf dem Netzwerk!");
                        PlayerData playerdata = new PlayerData(p.getName());
                        while (true) {
                            try {
                                String line = ChatColor.translateAlternateColorCodes('&', Config.settings.getString("Check.hover2." + i)).replace("%ip%", (playerdata.getLastip() == null || playerdata.getLastip().equals("")) ? MegaCord.fehler + "War noch nie hier :/" : ((sender.hasPermission("bungeecord.ip") || sender.hasPermission("bungeecord.*")) ? playerdata.getLastip() : "§k123.123.123.123")).replace("%firstJoin%", playerdata.getFirstjoin() == 0 ? MegaCord.fehler + "War noch nie hier :/" : (MegaCord.formatTime(playerdata.getFirstjoin()))).replace("%lastOnline%", playerdata.getLastonline() == 0 ? MegaCord.fehler + "War noch nie hier :/" : (playerdata.getLastonline() == -1 ? "Ist das erste mal hier ;)" : (targetConnected ? "Ist gerade Online :)" : MegaCord.formatTime(playerdata.getLastonline())))).replace("%reportsMade%", playerdata.getReportsMade() + "").replace("%warnsReceive%", playerdata.getWarnsReceive() + "").replace("%bansReceive%", playerdata.getBansReceive() + "").replace("%warnsMade%", playerdata.getWarnsMade() + "").replace("%bansMade%", playerdata.getBansMade() + "");
                                hoverArray.add(line);
                                i++;
                                if (i > 9) {
                                    hoverArray.remove(0);
                                    break;
                                }
                            } catch (Exception e1) {
                                break;
                            }
                        }

                        tc1.setText(MegaCord.other2 + "[" + MegaCord.fehler + "MEHR" + MegaCord.other2 + "]");
                        tc1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(String.join("\n", hoverArray))));
                        tc.addExtra(tc1);

                        Onlinezeit onlinezeit = new Onlinezeit(sender, MegaCord.getInstance().getDataSource());
                        if (!(sender instanceof ProxiedPlayer)) {
                            String h = hoverArray.get(0);
                            h = "\n" + h + "\n";
                            hoverArray.remove(0);
                            sender.sendMessage(new TextComponent(tc.getText() + h + String.join("\n", hoverArray)));

//                            ONLINEZEIT CONSOLE
                            onlinezeit.sendTrend(p.getName(), 7, true, ChatColor.translateAlternateColorCodes('&', Config.settings.getString("Check.onlinezeit")));
                            return;
                        }

                        sender.sendMessage(tc);
                        resetTC();

//                        ONLINEZEIT
                        onlinezeit.sendTrend(p.getName(), 7, false, ChatColor.translateAlternateColorCodes('&', Config.settings.getString("Check.onlinezeit")));
                    });
                });
            });
        });
    }
}