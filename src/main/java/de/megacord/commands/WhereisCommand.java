package de.megacord.commands;

import de.megacord.MegaCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class WhereisCommand extends Command {

    public WhereisCommand() {
        super("whereis", "megacord.command.whereis", "find");
    }

    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer)sender;
            if (!p.hasPermission("megacord.command.whereis")) {
                p.sendMessage(new TextComponent(MegaCord.Prefix + "§4Du hast keine Rechte!"));
                return;
            }

            ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
            if (target == null) {
                p.sendMessage(MegaCord.Prefix + "§4Der Spieler ist nicht auf dem Netzwerk!");
                return;
            }

            p.sendMessage(MegaCord.Prefix + "§7Der Spieler: §6" + target.getName() + "§b befindet dich derzeit auf: " + target.getServer().getInfo().getName() + "§7.");
        }

    }
}
