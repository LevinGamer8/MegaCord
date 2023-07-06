package de.megacord.commands;

import de.megacord.MegaCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class JumpCommand extends Command {
    public JumpCommand(){
        super("jump", "megacord.command.jump", "jumpto");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (!(sender instanceof ProxiedPlayer)) {
            return;
        }

        if (!(args.length == 1)) {
            sender.sendMessage(MegaCord.Prefix + "§4Nutze /Jump [Spieler]");
            return;
        }

        ProxiedPlayer p = (ProxiedPlayer) sender;
        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);

        if (target == null) {
            return;
        }

        if (target.getServer().getInfo().canAccess(sender)) {

            if (target.getServer().getInfo().equals(p.getServer().getInfo())) {
                sender.sendMessage(MegaCord.Prefix + "§4Du bist bereits auf dem selben §bServer §4wie §6 " + target.getName());
                return;
            }
            p.connect(target.getServer().getInfo());
            p.sendMessage(MegaCord.Prefix + "§bDu wurdest zu §6" + target.getName() + " §aauf " + target.getServer().getInfo().getName() + " §bgesendet.");

        } else {
            sender.sendMessage(MegaCord.Prefix + "§4Du darfst diesen Server nicht betreten!");
        }
    }
}
