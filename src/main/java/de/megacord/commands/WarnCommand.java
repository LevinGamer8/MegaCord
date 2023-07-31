package de.megacord.commands;

import de.megacord.MegaCord;
import de.megacord.utils.BanUtils;
import de.megacord.utils.Config;
import de.megacord.utils.DBUtil;
import de.megacord.utils.WarnManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;

public class WarnCommand extends Command {

    public WarnCommand() {
        super("warn");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            final ProxiedPlayer pp = (ProxiedPlayer) sender;
            if (pp.hasPermission("megacord.punish.warn") || pp.hasPermission("megacord.*")) {
                if (args.length >= 1) {
                    if (args[0].equalsIgnoreCase("del")) {
                        String id = args[1];
                        WarnManager warnManager = new WarnManager();
                        warnManager.setSource(MegaCord.getInstance().getDataSource());
                        warnManager.deleteWarn(id);
                        pp.sendMessage(new TextComponent(MegaCord.Prefix + "Der Warn wurde gelöscht!"));
                        return;
                    }
                    if (ProxyServer.getInstance().getPlayer(args[0]) == null) {
                        pp.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Der Spieler ist nicht online"));
                        return;
                    }
                    ProxiedPlayer pt = ProxyServer.getInstance().getPlayer(args[0]);
                    String grund = "";
                    for (int i = 1; i < args.length; i++) {
                        grund = grund + args[i] + " ";
                    }
                    WarnManager warnManager = new WarnManager(pt.getName(), pp.getName(), grund, System.currentTimeMillis(), Config.settings, MegaCord.getInstance().getDataSource());
                    warnManager.addWarn();
                    pp.sendMessage(new TextComponent(MegaCord.Prefix + "Du hast " + MegaCord.herH + args[0] + MegaCord.normal + " für " + MegaCord.herH + grund + MegaCord.normal + " gewanrt!"));
                    int maxWarns = Config.settings.getInt("Warns.MaxWarns");
                    ArrayList<String> warnArray = new ArrayList<>();
                    final int[] i = {1};
                    String finalGrund = grund;
                    DBUtil.getWhatCount(MegaCord.getInstance().getDataSource(), pt.getName(), "warn", true).whenComplete((whatCountTarget, ex) -> {
                        while (true) {
                            try {
                                String line = ChatColor.translateAlternateColorCodes('&', Config.settings.getString("WarnMessage.line" + i[0])).replace("%warnCount%", String.valueOf(whatCountTarget)).replace("%maxWarns%", String.valueOf(maxWarns)).replace("%grund%", finalGrund);
                                warnArray.add(line);
                                i[0]++;
                                if (i[0] > Config.settings.getInt("WarnMessage.lines"))
                                    break;
                            } catch (Exception e1) {
                                break;
                            }
                        }
                        pt.disconnect(new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', String.join("\n", warnArray))).create());
                        if (whatCountTarget >= maxWarns) {
                            new BanUtils(pt.getUniqueId().toString(), null, MegaCord.getInstance().getDataSource(), Config.settings, Config.standardBans).banByStandard(3, pt.getSocketAddress().toString().replace("/", "").split(":")[0]);
                            warnManager.deleteAllWarns();
                            pt.disconnect(new TextComponent(Config.settings.getString("BanDisconnected").replace("%absatz%", "\n").replace("%reason%", finalGrund)));
                            pp.sendMessage(new TextComponent(MegaCord.Prefix + "Der Spieler wurde, für mehr als " + MegaCord.herH + maxWarns + MegaCord.normal + " Warnungen, gebannt!"));
                        }
                    });
                } //else
                    //Help Message
            } else
                pp.sendMessage(new TextComponent(MegaCord.noPerm));
        } else
            sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Du bist kein Spieler!"));
    }
}

