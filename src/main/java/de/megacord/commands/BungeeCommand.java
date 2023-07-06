package de.megacord.commands;

import de.megacord.MegaCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class BungeeCommand extends Command {

    public BungeeCommand() {
        super("bungee", "", "mega");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(MegaCord.Prefix + "§3Mega§6Cord §7v. §a1.2");
    }
}
