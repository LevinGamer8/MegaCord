package de.megacord.utils;

import de.megacord.commands.*;
import de.megacord.listener.*;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.logging.Level;

public class Registry {

    private final Plugin plugin;
    private final Configuration settings;
    private final Configuration blacklist;
    private final Configuration standardBans;
    private HashMap<ProxiedPlayer, ProxiedPlayer> activechats = new HashMap<>();
    private final DataSource dataSource;



    public Registry(Plugin plugin, DataSource dataSource, Configuration settings, Configuration blacklist, Configuration standardBans, HashMap activechats) {
        this.plugin = plugin;
        this.dataSource = dataSource;
        this.settings = settings;
        this.blacklist = blacklist;
        this.standardBans = standardBans;
        this.activechats = activechats;
    }

    public void registerCommands() {
        PluginManager pluginManager = plugin.getProxy().getPluginManager();
        plugin.getLogger().log(Level.INFO, "Befehle werden registriert");
        pluginManager.registerCommand(plugin, new AccountCommand());
        pluginManager.registerCommand(plugin, new BroadcastCommand());
        pluginManager.registerCommand(plugin, new ReportCommand());
        pluginManager.registerCommand(plugin, new ReportsCommand());
        pluginManager.registerCommand(plugin, new KickCommand());
        pluginManager.registerCommand(plugin, new BanCommand());
        pluginManager.registerCommand(plugin, new BanEditCommand());
        pluginManager.registerCommand(plugin, new BanAddCommand());
        pluginManager.registerCommand(plugin, new BanRemoveCommand());
        pluginManager.registerCommand(plugin, new BanIDEditCommand());
        pluginManager.registerCommand(plugin, new UnbanCommand(dataSource));
        pluginManager.registerCommand(plugin, new BanListCommand());
        pluginManager.registerCommand(plugin, new CheckCommand());
        pluginManager.registerCommand(plugin, new WarnCommand());
        pluginManager.registerCommand(plugin, new WarnsCommand());
        pluginManager.registerCommand(plugin, new UUIDCommand());
        pluginManager.registerCommand(plugin, new FirstIPCommand());
        pluginManager.registerCommand(plugin, new LastIPCommand());
        pluginManager.registerCommand(plugin, new ReportsClear());
        pluginManager.registerCommand(plugin, new ChatClearCommand());
        pluginManager.registerCommand(plugin, new PingCommand());
        pluginManager.registerCommand(plugin, new KickallCommand());
        pluginManager.registerCommand(plugin, new WhereisCommand());
        pluginManager.registerCommand(plugin, new MSGCommand());
        pluginManager.registerCommand(plugin, new OnlineZeitCommand());
        pluginManager.registerCommand(plugin, new BungeeCommand());
        pluginManager.registerCommand(plugin, new JumpCommand());
        pluginManager.registerCommand(plugin, new setMaxIP());
        plugin.getLogger().log(Level.INFO, "Befehle wurden erfolgreich registriert");
    }

    public void registerListeners() {
        PluginManager pluginManager = plugin.getProxy().getPluginManager();
        plugin.getLogger().log(Level.INFO, "Listener werden registriert");
        pluginManager.registerListener(plugin, new TeamChatListener());
        pluginManager.registerListener(plugin, new PlayerJoinListener(plugin, dataSource, settings, standardBans));
        pluginManager.registerListener(plugin, new TabCompleteListener(plugin));
        pluginManager.registerListener(plugin, new BanAddListener(plugin));
        pluginManager.registerListener(plugin, new ChatListener(plugin, settings, blacklist, dataSource, standardBans, activechats));
        pluginManager.registerListener(plugin, new PlayerDisconnectListener(plugin, dataSource));
        plugin.getLogger().log(Level.INFO, "Listener wurden erfolgreich registriert");
    }

}
