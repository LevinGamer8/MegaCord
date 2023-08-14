package de.megacord.commands;

import de.megacord.MegaCord;
import de.megacord.utils.BanUtils;
import de.megacord.utils.Config;
import de.megacord.utils.DateUnit;
import de.megacord.utils.PlayerData;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class BanCommand extends Command {

    private ArrayList<Integer> bans = new ArrayList<>();
    private ArrayList<Integer> mutes = new ArrayList<>();
    private ArrayList<Integer> permas = new ArrayList<>();


    public BanCommand() {
        super("ban", "megacord.punish.ban", "mute");
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender.hasPermission("megacord.punish.ban") || sender.hasPermission("megacord.*")) {
            if (args.length == 2 || args.length == 3) {

                if (args[0] == null) {
                    return;
                }

                String targetName = args[0];

                PlayerData pl = new PlayerData(targetName);

                BanUtils currentBan = new BanUtils(targetName, pl.getLastip(), MegaCord.getInstance().getDataSource(), Config.settings, Config.standardBans);
                currentBan.isBanned(targetName).whenComplete((result, ex) -> {
                    int banid = 0;
                    try {
                        banid = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Gebe eine Zahl ein!"));
                        return;
                    }
                    String grund = Config.ban.getString("BanIDs." + banid + ".Reason");

                    int finalBanid = banid;
                    currentBan.getBanCount(grund, true).whenComplete((banCountResult, exception) -> {
                        if (!Config.ban.getSection("BanIDs").contains(finalBanid + "")) {
                            sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Diese ID existiert nicht!"));
                            return;
                        }
                        String banIdPerm = Config.ban.getString("BanIDs." + finalBanid + ".Permission");
                        if (!banIdPerm.equals("")) {
                            if (!sender.hasPermission("megacord.*")) {
                                if (!sender.hasPermission(banIdPerm)) {
                                    sender.sendMessage(new TextComponent(MegaCord.noPerm + MegaCord.other2 + " (" + MegaCord.herH + banIdPerm + MegaCord.other2 + ")"));
                                    return;
                                }
                            }
                        }

                        boolean perma = Config.ban.getBoolean("BanIDs." + finalBanid + ".Perma");
                        boolean ban = Config.ban.getBoolean("BanIDs." + finalBanid + ".Ban");
                        int permaint = 0;
                        int banint = 0;
                        if (perma) {
                            permaint = 1;
                        }
                        if (ban) {
                            banint = 1;
                        }
                        if (result) {
                            if (currentBan.getBan() == 1 && !ban) {
                                sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Wenn der Spieler gebannt ist bringt ein Mute auch nichts mehr!"));
                                return;
                            }
                            currentBan.unban(false, "PLUGIN"); // erstmal //
                            // hier abfragen, ob er den aktuellen Ban verändern will?
                        }
                        DateUnit unit;
                        try {
                            unit = DateUnit.valueOf((Config.ban.getString("BanIDs." + finalBanid + ".Format")).toUpperCase());
                        } catch (IllegalArgumentException | NullPointerException e) {
                            sender.sendMessage(new TextComponent(MegaCord.herH + (Config.ban.getString("BanIDs." + finalBanid + ".Format")) + MegaCord.fehler + " ist keine gültiges Format!"));
                            sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.normal + "Gültige Einheiten: "));
                            for (DateUnit date : DateUnit.values()) {
                                sender.sendMessage(new TextComponent(MegaCord.herH + date));
                            }
                            return;
                        }
                        long current = System.currentTimeMillis();
                        int time = Config.ban.getInt("BanIDs." + finalBanid + ".Time");

                        int banCount = (banCountResult + 1);
                        long millis = 0;
                        double y = 0;

                        if (Config.settings.getBoolean("Ban.permaafter3")) {
                            if (banCount > 3)
                                permaint = 1;
                        }
                        double pow = Math.pow(2, banCount);
                        y = time * pow;
                        if (banCount == 1)
                            y = y - time;
                        millis = Math.round(y * (unit.getToSec() * 1000));

                        long unban = current + millis;
                        if (permaint == 1)
                            unban = -1;
                        String beweis = "/";
                        if (args.length == 3) {
                            beweis = args[2];
                        }
                        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(targetName);
                        new BanUtils(targetName, sender.getName(), grund, System.currentTimeMillis(), unban, permaint, banint, target != null ? target.getSocketAddress().toString() : "NULL", beweis, MegaCord.getInstance().getDataSource(), Config.settings, Config.standardBans);
                    });
                });
            } else {
                if (Config.settings.getBoolean("BanPlaceholder.aktive"))
                    sendBans(sender);
                else
                    sendBanHelp(sender);
            }
        } else
            sender.sendMessage(new TextComponent(MegaCord.noPerm));
    }

    private void sortBans(CommandSender sender) {
        mutes.clear();
        bans.clear();
        permas.clear();
        for (int banID : Config.ban.getSection("BanIDs").getKeys().stream().map(Integer::parseInt).sorted().collect(Collectors.toList())) {
            String perm = Config.ban.getString("BanIDs." + banID + ".Permission");
            if (!perm.equalsIgnoreCase("")) {
                if (!sender.hasPermission("megacord.*")) {
                    if (!sender.hasPermission(perm))
                        continue;
                }
            }
            if (!Config.ban.getBoolean("BanIDs." + banID + ".Ban") && !Config.ban.getBoolean("BanIDs." + banID + ".Perma")) {
                mutes.add(banID);
            }
            if (Config.ban.getBoolean("BanIDs." + banID + ".Ban") && !Config.ban.getBoolean("BanIDs." + banID + ".Perma")) {
                bans.add(banID);
            }
            if (Config.ban.getBoolean("BanIDs." + banID + ".Perma")) {
                permas.add(banID);
            }
        }
    }

    private void sendBans(CommandSender sender) {
        sortBans(sender);
        for (int i = 1; i < 7; i++) {
            String message = "";
            switch (Config.settings.getString("BanPlaceholder.line" + i)) {
                case "%bans%":
                    for (int banID : bans) {
                        message = Config.settings.getString("BanReasons");
                        message = message.replace("%id%", banID + ".").replace("%reason%", Config.ban.getString("BanIDs." + banID + ".Reason")).replace("%time%", (Config.ban.getBoolean("BanIDs." + banID + ".Perma") ? "§4Permanent" : Config.ban.getInt("BanIDs." + banID + ".Time") + " " + Config.ban.getString("BanIDs." + banID + ".Format"))).replace("%status%", (Config.ban.getBoolean("BanIDs." + banID + ".Ban") ? "Ban" : "Mute"));
                        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
                    }
                    break;
                case "%mutes%":
                    for (int banID : mutes) {
                        message = Config.settings.getString("BanReasons");
                        message = message.replace("%id%", banID + ".").replace("%reason%", Config.ban.getString("BanIDs." + banID + ".Reason")).replace("%time%", (Config.ban.getBoolean("BanIDs." + banID + ".Perma") ? "§4Permanent" : Config.ban.getInt("BanIDs." + banID + ".Time") + " " + Config.ban.getString("BanIDs." + banID + ".Format"))).replace("%status%", (Config.ban.getBoolean("BanIDs." + banID + ".Ban") ? "Ban" : "Mute"));
                        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
                    }
                    break;
                case "%permas%":
                    for (int banID : permas) {
                        message = Config.settings.getString("BanReasons");
                        message = message.replace("%id%", banID + ".").replace("%reason%", Config.ban.getString("BanIDs." + banID + ".Reason")).replace("%time%", (Config.ban.getBoolean("BanIDs." + banID + ".Perma") ? "§4Permanent" : Config.ban.getInt("BanIDs." + banID + ".Time") + " " + Config.ban.getString("BanIDs." + banID + ".Format"))).replace("%status%", (Config.ban.getBoolean("BanIDs." + banID + ".Ban") ? "Ban" : "Mute"));
                        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
                    }
                    break;
                default:
                    sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', Config.settings.getString("BanPlaceholder.line" + i))));
                    break;
            }
        }

    }

    private void sendBanHelp(CommandSender sender) {
        for (int banID : Config.ban.getSection("BanIDs").getKeys().stream().map(Integer::parseInt).sorted().collect(Collectors.toList())) {

            String perm = Config.ban.getString("BanIDs." + banID + ".Permission");
            if (!perm.equalsIgnoreCase("")) {
                if (!sender.hasPermission("megacord.*")) {
                    if (!sender.hasPermission(perm))
                        continue;
                }
            }

            String message = Config.settings.getString("BanReasons");
            message = message.replace("%id%", banID + ".").replace("%reason%", Config.ban.getString("BanIDs." + banID + ".Reason")).replace("%time%", (Config.ban.getBoolean("BanIDs." + banID + ".Perma") ? "§4Permanent" : Config.ban.getInt("BanIDs." + banID + ".Time") + " " + Config.ban.getString("BanIDs." + banID + ".Format"))).replace("%status%", (Config.ban.getBoolean("BanIDs." + banID + ".Ban") ? "Ban" : "Mute"));
            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
        }

        if (Config.ban.getSection("BanIDs").getKeys().size() == 0) {
            sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Es wurden keine Ban-IDs gefunden!"));
            sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Benutze: " + MegaCord.other + "/banadd <ID>"));
            return;
        }
        sender.sendMessage(new TextComponent(MegaCord.fehler + "Benutze: " + MegaCord.other + "/ban <Spieler> <Ban-ID>"));
    }


}