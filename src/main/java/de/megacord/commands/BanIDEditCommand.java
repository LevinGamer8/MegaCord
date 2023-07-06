package de.megacord.commands;

import de.megacord.MegaCord;
import de.megacord.utils.Config;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.IOException;
import java.util.logging.Level;

public class BanIDEditCommand extends Command {

    public BanIDEditCommand() {
        super("Banidedit");
    }

    private String reason, reason2;
    private int time, time2;
    private String format, format2;
    private boolean ban, ban2;
    private boolean perma, perma2;
    private boolean report, report2;

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer) {
            final ProxiedPlayer pp = (ProxiedPlayer) sender;
            if(pp.hasPermission("megacord.punish.changeid") || pp.hasPermission("megacord.*")) {
                if(args.length == 2) {
                    try {
                        int oldID = Integer.parseInt(args[0]);
                        int newID = Integer.parseInt(args[1]);
                        if(idExists(oldID)) {
                            setNewID(oldID, newID, pp);
                        } else
                            pp.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler+"Die ID "+ MegaCord.herH+oldID+ MegaCord.fehler+" existiert nicht!"));
                    } catch (NumberFormatException e) {
                        pp.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler+"Gebe eine Zahl ein!"));
                    }
                } //else
                    // Help Message
            } else
                pp.sendMessage(new TextComponent(MegaCord.noPerm));
        } else
            sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler+"Du bist kein Spieler!"));
    }

    private void setNewID(int oldID, int newID, ProxiedPlayer pp) {
        if(idExists(newID)) {
            copyReason(oldID, true);
            copyReason(newID, false);
            addBan(reason, time, newID, format, ban, perma, report);
            addBan(reason2, time2, oldID, format2, ban2, perma2, report2);
            pp.sendMessage(new TextComponent(MegaCord.Prefix + "BanID "+ MegaCord.herH + oldID + MegaCord.normal+" wurde mit der BanID "+ MegaCord.herH + newID + MegaCord.normal+" getauscht!"));
        } else {
            copyReason(oldID, true);
            addBan(reason, time, newID, format, ban, perma, report);
            BanRemoveCommand.removeBan(oldID);
            pp.sendMessage(new TextComponent(MegaCord.Prefix + "BanID "+ MegaCord.herH + oldID + MegaCord.normal+" wurde die ID "+ MegaCord.herH + newID + MegaCord.normal+" zugewiesen"));
        }
    }

    private boolean idExists(int id) {
        if (!Config.ban.getSection("BanIDs").contains(id + "")) {
            return false;
        }
        return true;
    }

    private void copyReason(int id, boolean caze) {
        if(caze) {
            reason = Config.ban.getString("BanIDs." + id + ".Reason");
            time = Config.ban.getInt("BanIDs." + id + ".Time");
            format = Config.ban.getString("BanIDs." + id + ".Format");
            ban = Config.ban.getBoolean("BanIDs." + id + ".Ban");
            perma = Config.ban.getBoolean("BanIDs." + id + ".Perma");
            report = Config.ban.getBoolean("BanIDs." + id + ".Reportable");
        } else {
            reason2 = Config.ban.getString("BanIDs." + id + ".Reason");
            time2 = Config.ban.getInt("BanIDs." + id + ".Time");
            format2 = Config.ban.getString("BanIDs." + id + ".Format");
            ban2 = Config.ban.getBoolean("BanIDs." + id + ".Ban");
            perma2 = Config.ban.getBoolean("BanIDs." + id + ".Perma");
            report2 = Config.ban.getBoolean("BanIDs." + id + ".Reportable");
        }

    }

    private void addBan(String grund, int dauer, int id, String format, boolean banOrMute, boolean perma, boolean report) {
        Config.ban.set("BanIDs."+id + ".Reason", grund);
        Config.ban.set("BanIDs."+id + ".Time", dauer);
        Config.ban.set("BanIDs."+id + ".Format", format);
        Config.ban.set("BanIDs."+id + ".Ban", banOrMute);
        Config.ban.set("BanIDs."+id + ".Perma", perma);
        Config.ban.set("BanIDs." + id + ".Reportable", report);
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(Config.ban, Config.banFile);
        } catch (IOException e) {
            MegaCord.logger().log(Level.WARNING, "cloud not change ban id for id " + id, e);
        }
    }

}