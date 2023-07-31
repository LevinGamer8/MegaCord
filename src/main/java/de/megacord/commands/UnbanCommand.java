package de.megacord.commands;

import de.megacord.MegaCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

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
                try (Connection conn = getSource().getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM bannedPlayers WHERE TargetName = ?")) {
                    ps.setString(1, args[0]);
                    ps.executeUpdate();
                    sender.sendMessage(MegaCord.Prefix + "§aDer §6Spieler §awurde erfolgreich entbannt!");
                } catch (SQLException e) {
                    MegaCord.logger().log(Level.WARNING, "could not delete player from bannedplayer-Table", e);
                }
            }
        } else
            sender.sendMessage(new TextComponent(MegaCord.noPerm));
    }

    public DataSource getSource() {
        return source;
    }

}
