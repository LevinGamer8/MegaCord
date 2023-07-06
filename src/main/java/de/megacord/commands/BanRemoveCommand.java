package de.megacord.commands;

import de.megacord.MegaCord;
import de.megacord.utils.Config;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.IOException;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BanRemoveCommand extends Command {

    public BanRemoveCommand() {
        super("banremove");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            final ProxiedPlayer pp = (ProxiedPlayer) sender;
            if (pp.hasPermission("megacord.punish.ban.remove") || pp.hasPermission("megacord.*")) {
                if (args.length == 1) {
                    int id = 0;
                    try {
                        id = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        if (args[0].equalsIgnoreCase("all")) {
                            for (int banID : Config.ban.getSection("BanIDs").getKeys().stream().map(Integer::parseInt).sorted().collect(Collectors.toList())) {
                                removeBan(banID);
                            }
                            pp.sendMessage(MegaCord.Prefix + "Alle Ban-IDs wurden gelöscht!");
                        } else
                            pp.sendMessage(MegaCord.Prefix + MegaCord.fehler +"Gebe eine Zahl ein!");
                        return;
                    }
                    if (!Config.ban.getSection("BanIDs").contains(id + "")) {
                        pp.sendMessage(MegaCord.Prefix + MegaCord.fehler + "Diese ID existiert nicht!");
                        return;
                    }
                    removeBan(id);
                    pp.sendMessage(MegaCord.Prefix + "Die ID "+ MegaCord.herH + id + MegaCord.normal +" wurde entfernt!");
                }
            } else
                pp.sendMessage(MegaCord.noPerm);
        } else
            sender.sendMessage(MegaCord.Prefix + MegaCord.fehler + "Du bist kein Spieler!");
    }

    public static void removeBan(int id) {
        Config.ban.set("BanIDs." + id + ".Reason", null);
        Config.ban.set("BanIDs." + id + ".Time", null);
        Config.ban.set("BanIDs." + id + ".Format", null);
        Config.ban.set("BanIDs." + id + ".BanUtil", null);
        Config.ban.set("BanIDs." + id + ".Perma", null);
        Config.ban.set("BanIDs." + id + ".Reportable", null);
        Config.ban.set("BanIDs." + id + "", null);
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(Config.ban, Config.banFile);
        } catch (IOException e) {
            MegaCord.logger().log(Level.WARNING, "could not save removed ban", e);
        }
    }

}
