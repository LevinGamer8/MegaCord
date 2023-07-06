package de.megacord.commands;

import de.megacord.MegaCord;
import de.megacord.utils.BanUtils;
import de.megacord.utils.Config;
import de.megacord.utils.UUIDFetcher;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;

public class UnbanCommand extends Command {

    public UnbanCommand() {
        super("unban", "megacord.punish.unban", "unmute");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender.hasPermission("megacord.punish.unban") || sender.hasPermission("megacord.*")) {
            if (args.length == 1) {
                UUID pt = UUIDFetcher.getUUID(args[0]);
                if (pt == null) {
                    sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Dieser Spieler existiert nicht!"));
                    return;
                }
                BanUtils ban = new BanUtils(pt, null, MegaCord.getInstance().getDataSource(), Config.settings, Config.standardBans);
                ban.isBanned().whenComplete((bannedResult, exception) -> {
                    if (bannedResult) {
                        ban.unban(true, sender instanceof ProxiedPlayer ? sender.getName() : "CONSOLE").whenComplete((result, ex) -> {
                            if (!result)
                                sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Ein Fehler ist aufgetreten!"));
                        });
                    } else
                        sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Dieser Spieler ist nicht gebannt!"));
                });
            }
        } else
            sender.sendMessage(new TextComponent(MegaCord.noPerm));
    }

}
