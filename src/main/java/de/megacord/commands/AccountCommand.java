package de.megacord.commands;

import de.megacord.MegaCord;
import de.megacord.utils.PlayerData;
import de.megacord.utils.UUIDFetcher;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

public class AccountCommand extends Command {

    public AccountCommand() {
        super("accounts", "megacord.punish.accounts", "alt", "alts");
    }

    private ArrayList<String> ips = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> warschon = new ArrayList<>();


    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender.hasPermission("megacord.punish.accounts") || sender.hasPermission("megacord.*")) {
            if (args.length == 0) {
                sendAccounts(sender);
            } else if (args.length == 1) {
                String lastIP = args[0];
                String[] lastIPSplit = lastIP.split("\\.");

                if (lastIPSplit.length < 3) {
                    sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Bitte verwende eine richtige IP (XXX.XXX.XXX)"));
                    return;
                }

                ProxiedPlayer p = ProxyServer.getInstance().getPlayer(args[0]);
                if (p == null) {
                    sender.sendMessage(new TextComponent(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Gebe einen richtigen Spieler an!")));
                    return;
                }
                PlayerData playerdata = new PlayerData(p.getName());
                lastIP = playerdata.getLastip();
                if (lastIP == null || lastIP == "") {
                    sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Dieser Spieler war noch nie auf dem Netzwerk!"));
                    return;
                }
                if (lastIPSplit.length >= 3) {
                    try {
                        //Split

                        lastIP = lastIPSplit[0] + "." + lastIPSplit[1] + "." + lastIPSplit[2];

                    } catch (ArrayIndexOutOfBoundsException e) {
                        sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Bitte verwende eine richtige IP (XXX.XXX.XXX)"));
                        return;
                    }

                    int count = 0;
                    try (Connection conn = MegaCord.getInstance().getDataSource().getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM playerdata WHERE lastIP LIKE '%" + lastIP + "%'");) {
                        ResultSet rs = ps.executeQuery();
                        sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.normal + "Accounts von " + MegaCord.herH + args[0]));
                        while (rs.next()) {
                            TextComponent tc = new TextComponent();
                            String name = rs.getString("Name");
                            if (!name.equalsIgnoreCase(args[0])) {
                                tc.setText(MegaCord.Prefix + MegaCord.herH + name);
                                tc.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ban " + name + " "));
                                tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MegaCord.fehler + name + MegaCord.normal + " bannen?")));
                                if (sender instanceof ProxiedPlayer) {
                                    sender.sendMessage(tc);
                                    count++;
                                } else
                                    sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.herH + name));
                            }
                        }
                    } catch (SQLException e) {
                        MegaCord.logger().log(Level.WARNING, "could not read account data", e);
                    }
                    if (count <= 0)
                        sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Keine Alt Accounts gefunden!"));
                } else {
                    sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Nutzung: /alts (Spielername/IP)"));
                }
            } else
                sender.sendMessage(new TextComponent(MegaCord.noPerm));
        }
    }
    private void sendAccounts(CommandSender sender) {
        try (Connection conn = MegaCord.getInstance().getDataSource().getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT lastIP, Name FROM playerdata GROUP BY lastIP HAVING COUNT(lastIP) > 1")) {
            ips.clear();
            warschon.clear();

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Es konnten keine Alt-Accounts gefunden werden!"));
                return;
            }

            while (rs.next()) {
                names.clear();
                String lastIP = rs.getString("lastIP");
                final String name = rs.getString("Name");


                String[] lastIPSplit = lastIP.split("\\.");
                lastIP = lastIPSplit[0] + "." + lastIPSplit[1] + "." + lastIPSplit[2];

                if (!warschon.contains(lastIP)) {

                    try (Connection conn1 = MegaCord.getInstance().getDataSource().getConnection(); PreparedStatement ps1 = conn1.prepareStatement("SELECT lastIP,Name FROM playerdata WHERE lastIP LIKE '%" + lastIP + "%'")) {

                        ResultSet rs1 = ps1.executeQuery();
                        while (rs1.next()) {

                            names.add(MegaCord.normal + rs1.getString("Name"));
                        }
                    } catch (SQLException e) {
                        MegaCord.logger().log(Level.WARNING, "could not read names for /accounts", e);
                    }


                    TextComponent tc = new TextComponent();
                    tc.setText(MegaCord.herH + name + " ");
                    TextComponent tc1 = new TextComponent();
                    tc1.setText(MegaCord.other2 + "[" + MegaCord.fehler + "MEHR" + MegaCord.other2 + "]");
                    tc1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MegaCord.normal + "IP: " + MegaCord.herH + ((sender.hasPermission("megacord.command.ip") || sender.hasPermission("megacord.*")) ? lastIP + ".XXX" : "§k123.123.123") + "\n" + String.join("\n", names))));
                    tc.addExtra(tc1);
                    sender.sendMessage(tc);
                    warschon.add(lastIP);
                }
            }
        } catch (SQLException e) {
            MegaCord.logger().log(Level.WARNING, "could not read data for /accounts", e);
        }
    }

}