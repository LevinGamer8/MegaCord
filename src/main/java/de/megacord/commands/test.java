package de.megacord.commands;

import de.megacord.MegaCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class test extends Command {

    private MegaCord megaCord;

    public test(MegaCord megaCord) {
        super("test");
        this.megaCord = megaCord;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            return;
        }

        ProxiedPlayer p = (ProxiedPlayer) sender;

        megaCord.sendCustomData(p, "test", 1);
    }
}
