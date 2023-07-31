package de.megacord.commands;

import de.megacord.MegaCord;
import de.megacord.utils.PlayerData;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.SQLException;

public class setMaxIP extends Command {
    public setMaxIP() {
        super("setmaxip", "megacord.command.setmaxip");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(MegaCord.Prefix + MegaCord.fehler + "Dieser Command ist nur für Spieler!");
            return;
        }

        if (!(sender.hasPermission("megacord.command.setmaxip"))) {
            sender.sendMessage(MegaCord.Prefix + MegaCord.fehler + MegaCord.noPerm);
            return;
        }
        if (!(args.length == 2)) {
            sender.sendMessage(MegaCord.Prefix + MegaCord.fehler + "Nutze /setmaxip §b[ip] [maxIPs]");
            return;
        }

        ProxiedPlayer p = (ProxiedPlayer) sender;

        PlayerData pl = new PlayerData(p.getUniqueId());
        try {
            pl.setMaxIP(args[0], Integer.parseInt(args[1]));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
