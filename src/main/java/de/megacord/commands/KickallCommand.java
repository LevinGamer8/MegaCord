package de.megacord.commands;

import de.megacord.MegaCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Collections;
import java.util.stream.Collectors;

public class KickallCommand extends Command implements TabExecutor{

    public KickallCommand() {
            super("kickall", "megacord.command.kickall", "ka");
        }

        public void execute(CommandSender sender, String[] args) {
            if (args.length == 0) {
                sender.sendMessage(MegaCord.Prefix + ChatColor.DARK_RED + "Syntax: /kickall <server>");
            } else {
                ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(args[0]);
                if (serverInfo == null) {
                    sender.sendMessage(new TextComponent(MegaCord.Prefix + ChatColor.DARK_RED + "Der Server existiert nicht"));
                } else {
                    if (sender.hasPermission("megacord.kickall.bypass")) {
                        serverInfo.getPlayers().forEach((rec$) -> {
                            ((Connection)rec$).disconnect(new BaseComponent[0]);
                        });
                    }

                }
            }
        }

        public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
            return (Iterable)(args.length == 0 ? Collections.emptyList() : (Iterable)ProxyServer.getInstance().getServers().values().stream().map(ServerInfo::getName).filter((s) -> {
                return s.startsWith(args[0]);
            }).collect(Collectors.toList()));
        }
    }
