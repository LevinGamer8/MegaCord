package de.megacord.commands;

import de.megacord.MegaCord;
import de.megacord.utils.BanUtils;
import de.megacord.utils.Config;
import de.megacord.utils.PlayerData;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import javax.sql.DataSource;

public class UnbanCommand extends Command {

    private DataSource source;
    public UnbanCommand(DataSource source) {
        super("unban", "megacord.punish.unban", "unmute");
        this.source = source;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender.hasPermission("megacord.punish.unban") || sender.hasPermission("megacord.*")) {
            if (args.length == 1) {

                PlayerData pd = new PlayerData(args[0]);
                if (!(pd.exists())) {
                    sender.sendMessage(MegaCord.Prefix + "§4Der Spieler war noch nie auf dem Netzwerk!");
                    return;
                }

                BanUtils ban = new BanUtils(args[0], pd.getLastip(), MegaCord.getInstance().getDataSource(), Config.settings, Config.standardBans);

                ban.isBanned(pd.getName()).whenComplete((bannedResult, exception) -> {
                    if (bannedResult) {
                        ban.unban(true, sender instanceof ProxiedPlayer ? sender.getName() : "CONSOLE").whenComplete((result, ex) -> {
                            if (!result)
                                sender.sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Ein Fehler ist aufgetreten!"));
                        });
                    }
                });
            }
        } else
            sender.sendMessage(new TextComponent(MegaCord.noPerm));
    }

    public DataSource getSource() {
        return source;
    }

}
