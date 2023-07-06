package de.megacord.commands;

import de.megacord.MegaCord;
import de.megacord.utils.Config;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.IOException;
import java.util.logging.Level;

public class BanAddCommand extends Command {

    public BanAddCommand() {
        super("banadd");
    }

    public static int phase = 1;
    public static String grund = "";
    public static int ban = -1;
    public static int perma = -1;
    public static int dauer = -1;
    public static int report = -1;
    public static String format = "";
    public static String perm = "";
    private static int banID;
    public static boolean finished = false;
    public static ProxiedPlayer p;
    private static TextComponent hilfe = new TextComponent();
    private static TextComponent text = new TextComponent();
    private static TextComponent info = new TextComponent();

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            final ProxiedPlayer pp = (ProxiedPlayer) sender;
            if (pp.hasPermission("megacord.punish.ban.add") || pp.hasPermission("megacord.*")) {
                if (args.length == 1) {
                    try {
                        banID = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        pp.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Gebe eine Zahl ein!"));
                        return;
                    }
                    if (idExists(banID)) {
                        pp.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Diese ID existiert bereits!"));
                        return;
                    }
                    p = pp;
                    startSetup();
                }
            } else
                pp.sendMessage(new TextComponent(MegaCord.noPerm));
        } else
            sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Du bist kein Spieler!"));
    }

    private boolean idExists(int id) {
        if (!Config.ban.getSection("BanIDs").contains(id + "")) {
            return false;
        }
        return true;
    }

    public static void finishSetup() {
        p.sendMessage(new TextComponent(MegaCord.Prefix + "Der Ban wurde unter der ID " + MegaCord.herH + banID + MegaCord.normal + " erstellt!"));
        finished = true;

        Config.ban.set("BanIDs." + banID + ".Reason", grund);
        Config.ban.set("BanIDs." + banID + ".Time", dauer);
        Config.ban.set("BanIDs." + banID + ".Format", format);
        Config.ban.set("BanIDs." + banID + ".Ban", ban == 1);
        Config.ban.set("BanIDs." + banID + ".Perma", perma == 1);
        Config.ban.set("BanIDs." + banID + ".Reportable", report == 1);
        Config.ban.set("BanIDs." + banID + ".Permission", perm);
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(Config.ban, Config.banFile);
        } catch (IOException e) {
            MegaCord.logger().log(Level.WARNING, "cloud not update fileconfiguration for bans", e);
        }
    }

    public static void startPhase(int phase) {
        if (text.getExtra() != null)
            text.getExtra().clear();
        info.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MegaCord.normal + "ID: " + MegaCord.herH + banID + "\n" + MegaCord.normal + "Perma: " + MegaCord.herH + (perma == -1 ? "???" : (perma == 0 ? MegaCord.fehler + "Nein" : MegaCord.normal + "Ja")) + "\n" + MegaCord.normal + "Status: " + MegaCord.herH + (ban == -1 ? "???" : (ban == 0 ? "Mute" : "Ban")) + "\n" + MegaCord.normal + "Grund: " + MegaCord.herH + (grund.equalsIgnoreCase("") ? "???" : grund) + "\n" + MegaCord.normal + "Dauer: " + MegaCord.herH + (perma == 1 ? "Permanent" : (dauer == -1 ? "???" : dauer)) + "\n" + MegaCord.normal + "Format: " + MegaCord.herH + (perma == 1 ? "Permanent" : (format.equalsIgnoreCase("") ? "???" : format.toUpperCase())) + "\n" + MegaCord.normal + "Report: " + MegaCord.herH + (report == -1 ? "???" : (report == 0 ? MegaCord.fehler + "Nein" : MegaCord.normal + "Ja")) + "\n" + MegaCord.normal + "Permission: " + MegaCord.herH + (perm == "" ? "???" : perm))));
        switch (phase) {
            case 1: // PERMA?
                text.setText(MegaCord.normal + "Soll der Ban " + MegaCord.herH + "Permanent " + MegaCord.normal + "sein?");
                hilfe.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MegaCord.normal + "Schreibe in den Chat\n" + MegaCord.herH + "1 §f» " + MegaCord.normal + "Ja!\n" + MegaCord.herH + "0 §f» " + MegaCord.fehler + "Nein!")));
                break;
            case 2: // MUTE / BAN
                text.setText(MegaCord.normal + "Soll der Ban ein " + MegaCord.herH + "Mute " + MegaCord.normal + "oder ein " + MegaCord.herH + "Ban " + MegaCord.normal + "sein?");
                hilfe.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MegaCord.normal + "Schreibe in den Chat\n" + MegaCord.herH + "1 §f» " + MegaCord.herH + "Ban!\n" + MegaCord.herH + "0 §f» " + MegaCord.herH + "Mute!")));
                break;
            case 3: // GRUND
                text.setText(MegaCord.normal + "Wie soll der " + MegaCord.herH + "Grund " + MegaCord.normal + "lauten?");
                hilfe.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MegaCord.normal + "Beispiele:\n" + MegaCord.herH + "Ban eines Admins\n" + MegaCord.herH + "Hacking\n" + MegaCord.herH + "Hausverbot\n" + MegaCord.herH + "...")));
                break;
            case 4: // FORMAT
                if (perma == 1) {
                    BanAddCommand.phase = 6;
                    startPhase(BanAddCommand.phase);
                    return;
                }
                text.setText(MegaCord.normal + "In welchem " + MegaCord.herH + "Format " + MegaCord.herH + "soll der Ban gespeichert werden? " + MegaCord.other2 + "(siehe Hilfe)");
                hilfe.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MegaCord.normal + "Verfügbare Formate: \n" + MegaCord.herH + "MIN\n" + MegaCord.herH + "HOUR\n" + MegaCord.herH + "DAY\n" + MegaCord.herH + "WEEK\n" + MegaCord.herH + "MON\n" + MegaCord.herH + "YEAR")));
                break;
            case 5: // DAUER
                text.setText(MegaCord.herH + "Wie lang " + MegaCord.normal + "soll der Ban gehen?");
                hilfe.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MegaCord.normal + "Beispiele:\n" + MegaCord.herH + "1\n" + MegaCord.herH + "5 \n" + MegaCord.herH + "7 \n" + MegaCord.herH + "...\nDas Format wird dann automatisch rangesetzt!")));
                break;
            case 6: // REPORT
                text.setText(MegaCord.normal + "Soll dieser Ban als " + MegaCord.herH + "Report " + MegaCord.normal + "angegeben werden dürfen?");
                hilfe.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MegaCord.normal + "Schreibe in den Chat\n" + MegaCord.herH + "1 §f» " + MegaCord.normal + "Ja!\n" + MegaCord.herH + "0 §f» " + MegaCord.fehler + "Nein!")));
                if (perma == 1) {
                    format = "HOUR";
                    dauer = 10;
                }
                break;
            case 7: // PERMISSION?
                text.setText(MegaCord.normal + "Braucht man eine " + MegaCord.herH + "Permission" + MegaCord.normal + ", um die Ban-ID benutzen zu können?");
                hilfe.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MegaCord.normal + "Schreibe in den Chat\n" + MegaCord.herH + "0 §f» " + MegaCord.normal + "Keine!\n§f» " + MegaCord.normal + "Ansonsten einfach die Permission reinschreiben")));
                break;
        }
        text.addExtra(hilfe);
        text.addExtra(info);
        p.sendMessage(text);
    }

    private void startSetup() {
        reset();
        hilfe.setText(" " + MegaCord.other2 + "[" + MegaCord.fehler + "HILFE" + MegaCord.other2 + "]");
        info.setText(" " + MegaCord.other2 + "[" + MegaCord.fehler + "INFO" + MegaCord.other2 + "]");
        startPhase(phase);
    }

    private void reset() {
        phase = 1;
        grund = "";
        ban = -1;
        perma = -1;
        dauer = -1;
        report = -1;
        format = "";
        perm = "";
        finished = false;
        text = new TextComponent();
        hilfe = new TextComponent();
        info = new TextComponent();
    }

}
