package de.megacord.utils;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Config {

    private static Plugin plugin;
    public static Configuration mysqlConfig;
    public static Configuration ban;
    public static Configuration settings;
    public static Configuration cooldowns;
    public static Configuration blacklist;
    public static Configuration standardBans;
    public static File cooldownsFile;
    public static File standardBansFile;
    public static File banFile;
    public static File blacklistFile;

    public Config(Plugin plugin) {
        Config.plugin = plugin;
    }

    public static void loadConfig() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdir();
            }
            File settingsFile = new File(plugin.getDataFolder().getPath(), "settings.yml");
            banFile = new File(plugin.getDataFolder().getPath(), "reasons.yml");
            File mysqlFile = new File(plugin.getDataFolder().getPath(), "mysql.yml");
            cooldownsFile = new File(plugin.getDataFolder().getPath(), "cooldowns.yml");
            blacklistFile = new File(plugin.getDataFolder().getPath(), "blacklist.yml");
            standardBansFile = new File(plugin.getDataFolder().getPath(), "standardbans.yml");
            if (!mysqlFile.exists()) {
                mysqlFile.createNewFile();
                mysqlConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(mysqlFile);

                mysqlConfig.set("host", "localhost");
                mysqlConfig.set("port", 3308);
                mysqlConfig.set("database", "megacord");
                mysqlConfig.set("username", "user");
                mysqlConfig.set("password", "123");
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(mysqlConfig, mysqlFile);
            }
            mysqlConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(mysqlFile);

            if (!standardBansFile.exists()) {
                standardBansFile.createNewFile();
                standardBans = ConfigurationProvider.getProvider(YamlConfiguration.class).load(standardBansFile);

                standardBans.set("BanIDs.1.Reason", "Alt-Account");
                standardBans.set("BanIDs.1.Time", 10);
                standardBans.set("BanIDs.1.Format", "HOUR");
                standardBans.set("BanIDs.1.Ban", true);
                standardBans.set("BanIDs.1.Perma", true);

                standardBans.set("BanIDs.2.Reason", "Chatverhalten");
                standardBans.set("BanIDs.2.Time", 1);
                standardBans.set("BanIDs.2.Format", "MON");
                standardBans.set("BanIDs.2.Ban", false);
                standardBans.set("BanIDs.2.Perma", false);

                standardBans.set("BanIDs.3.Reason", "Warnungen");
                standardBans.set("BanIDs.3.Time", 3);
                standardBans.set("BanIDs.3.Format", "MON");
                standardBans.set("BanIDs.3.Ban", true);
                standardBans.set("BanIDs.3.Perma", false);
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(standardBans, standardBansFile);
            }
            standardBans = ConfigurationProvider.getProvider(YamlConfiguration.class).load(standardBansFile);

            if (!banFile.exists() || banFile == null) {
                banFile.createNewFile();
                ban = ConfigurationProvider.getProvider(YamlConfiguration.class).load(banFile);

                ban.set("BanIDs.1.Reason", "Clientmodifikation");
                ban.set("BanIDs.1.Time", 1);
                ban.set("BanIDs.1.Format", "MON");
                ban.set("BanIDs.1.Ban", true);
                ban.set("BanIDs.1.Perma", false);
                ban.set("BanIDs.1.Reportable", true);

                ban.set("BanIDs.2.Reason", "Chatverhalten");
                ban.set("BanIDs.2.Time", 3);
                ban.set("BanIDs.2.Format", "HOUR");
                ban.set("BanIDs.2.Ban", false);
                ban.set("BanIDs.2.Perma", false);
                ban.set("BanIDs.2.Reportable", true);

                ConfigurationProvider.getProvider(YamlConfiguration.class).save(ban, banFile);
            }
            ban = ConfigurationProvider.getProvider(YamlConfiguration.class).load(banFile);
            if (!settingsFile.exists()) {
                settingsFile.createNewFile();
                settings = ConfigurationProvider.getProvider(YamlConfiguration.class).load(settingsFile);

                settings.set("Prefix", "&3MegaCraft&7: &r");
                settings.set("NoPerm", "&4Dazu hast du keine Rechte!");
                settings.set("WarnInfo", "&b%player% &ahat &b%target% &afür &b%reason% &agewarnt!");
                settings.set("BanReasons", "&a%id% &f» &c%reason% &8- &b%time% &8(&6%status%&8)");
                settings.set("AntiAd", "&4Bitte mache keine Werbung!");
                settings.set("Onlinezeit", "&a%player% &f» %onlinezeit%");


                settings.set("ChatColor.normal", "&a");
                settings.set("ChatColor.fehler", "&4");
                settings.set("ChatColor.hervorhebung", "&b");
                settings.set("ChatColor.other", "&e");
                settings.set("ChatColor.other2", "&7");

                settings.set("Ban.Baninfo", "&b%player% &ahat &b%target% &afür &b%reason% &agebannt!");
                settings.set("Ban.permaafter3", true);
                settings.set("Ban.Unbaninfo", "&b%player% &ahat &b%target% &aentbannt!");
                settings.set("Ban.Editinfo", "&aDer Ban von &b%target% &awurde von &b%player% &aeditiert!");
                settings.set("Ban.Disconnectmessage", "§8-------------------------------%absatz% §3§lMegaCraft §e§lNetzwerk§r%absatz%§4Du wurdest §4§lgebannt!§r%absatz%§bGrund §8» §c%reason% %absatz%§8-------------------------------");
                settings.set("Ban.Usermessage", "&aDer Spieler &b%target% &awurde für &b%reason% &agebannt!");
                settings.set("Ban.Extrainfohover.1", "&bVon: &3%name%");
                settings.set("Ban.Extrainfohover.2", "&bGrund: &3%reason%");
                settings.set("Ban.Extrainfohover.3", "&bBis: &3%bis%");
                settings.set("Ban.Extrainfohover.4", "&bErstellt: &3%erstellt%");

                settings.set("Kick.Kickinfo", "&b%player% &ahat &b%target% &afür &b%reason% &agekickt!");
                settings.set("Kick.Disconnectmessage", "§8-------------------------------%absatz% §3§lMegaCraft §e§lNetzwerk§r%absatz%§4Du wurdest §4§lgekick!§r%absatz%§bGrund §8» §c%reason% %absatz%§8-------------------------------");
                settings.set("Kick.Usermessage", "&aDer Spieler &b%target% &awurde für &b%reason% &agekick!");
                settings.set("Kick.Extrainfohover.1", "&bVon: &3%name%");
                settings.set("Kick.Extrainfohover.2", "&bGrund: &3%reason%");
                settings.set("Kick.Extrainfohover.3", "&bErstellt: &3%erstellt%");

                settings.set("Chatlog.Oncooldown", "§4Du kannst erst bald einen weiteren Chatlog erstellen!");
                settings.set("Chatlog.created", "§aDer Chatlog wird nun weitergeleitet!");
                settings.set("Chatlog.userGetLink", false);
                settings.set("Chatlog.userGetLinkMessage", "§aDein Chatlog wird unter folgender URL &ageführt: &b%url%");

                settings.set("Check.status", "&aStatus: &b%status%");
                settings.set("Check.hover.1", "&dVon: &3%name%");
                settings.set("Check.hover.2", "&dGrund: &3%reason%");
                settings.set("Check.hover.3", "&dBis: &3%bis%");
                settings.set("Check.hover.4", "&dEditiert von: &3%editby%");
                settings.set("Check.reports", "&aReports: &b%reportCount%");
                settings.set("Check.warns", "&aWarns: &b%warnsCount%");
                settings.set("Check.bans", "&aBans: &b%bansCount%");
                settings.set("Check.history", "&aHistory: &b%historyCount%");
                settings.set("Check.stats", "&aStats: ");
                settings.set("Check.hover2.1", "&dIP: &3%ip%");
                settings.set("Check.hover2.2", "&dErstes mal Online: &3%firstJoin%");
                settings.set("Check.hover2.3", "&dLetztes mal Online: &3%lastOnline%");
                settings.set("Check.hover2.4", "&dReports Erstellt: &3%reportsMade%");
                settings.set("Check.hover2.5", "&dWarns Erhalten: &3%warnsReceive%");
                settings.set("Check.hover2.6", "&dBans Erhalten: &3%bansReceive%");
                settings.set("Check.hover2.7", "&dWarns Erstellt: &3%warnsMade%");
                settings.set("Check.hover2.8", "&dBans Erstellt: &3%bansMade%");
                settings.set("Check.onlinezeit", "&aOnlinezeit: ");

                settings.set("History.Message", "&8(&6%type%&8) &f» &d%grund% &8{&a%time%&8} &f» ");
                settings.set("History.hover.1", "&7Von: &b%von%");
                settings.set("History.hover.2", "&7Bis: &b%bis%");
                settings.set("History.hover.3", "&7Zeit: &b%zeit%");
                settings.set("History.hover.4", "&7Status: &b%status%");
                settings.set("History.hover.5", "&7Aktiv: &b%aktiv%");
                settings.set("History.hover.6", "&7Entbannt von: &b%entbanner%");

                settings.set("Cooldown.Report.aktive", false);
                settings.set("Cooldown.Report.time", 10);
                settings.set("Cooldown.Report.format", "MIN");
                settings.set("Cooldown.Chatlog.aktive", true);
                settings.set("Cooldown.Chatlog.time", 1);
                settings.set("Cooldown.Chatlog.format", "HOUR");
                settings.set("Cooldown.Support.aktive", false);
                settings.set("Cooldown.Support.time", 10);
                settings.set("Cooldown.Support.format", "MIN");

                settings.set("Reports.Message", "%grund% &f» ");
                settings.set("Reports.hover.1", "&7Wer: &b%wer%");
                settings.set("Reports.hover.2", "&7Von: &b%von%");
                settings.set("Reports.hover.3", "&7Datum: &b%time%");

                settings.set("Bans.Message", "%grund% &f» ");
                settings.set("Bans.hover.1", "&7Wer: &b%wer%");
                settings.set("Bans.hover.2", "&7Von: &b%von%");
                settings.set("Bans.hover.3", "&7Datum: &b%time%");
                settings.set("Bans.hover.4", "&7Bis: &b%bis%");
                settings.set("Bans.hover.5", "&7Status: &b%status%");
                settings.set("Bans.hover.6", "&7Aktiv: &b%aktiv%");
                settings.set("Bans.hover.7", "&7Entbannt von: &b%entbanner%");

                settings.set("Warns.MaxWarns", 3);
                settings.set("Warns.Message", "%grund% &f» ");
                settings.set("Warns.hover.1", "&7Wer: &b%wer%");
                settings.set("Warns.hover.2", "&7Von: &b%von%");
                settings.set("Warns.hover.3", "&7Datum: &b%time%");
                settings.set("MutedMessage", "&cDu wurdest aus dem Chat verbannt!%absatz%&cGrund: &b%grund%%absatz%&cBis: &b%bis%");

                settings.set("BanPlaceholder.aktive", false);
                settings.set("BanPlaceholder.line1", "&7&m----------- &bMUTES &7&m---------------");
                settings.set("BanPlaceholder.line2", "%mutes%");
                settings.set("BanPlaceholder.line3", "&7&m----------- &bBANS &7&m----------------");
                settings.set("BanPlaceholder.line4", "%bans%");
                settings.set("BanPlaceholder.line5", "&7&m----------- &bPERMA &7&m---------------");
                settings.set("BanPlaceholder.line6", "%permas%");

                settings.set("KickMessage.lines", 9);
                settings.set("KickMessage.line1", "&8-------------------------------");
                settings.set("KickMessage.line2", "");
                settings.set("KickMessage.line3", "&3&lMegaCraft &e&lNetzwerk&r");
                settings.set("KickMessage.line4", "");
                settings.set("KickMessage.line5", "&4Du wurdest &4&lgekickt§!&r");
                settings.set("KickMessage.line6", "");
                settings.set("KickMessage.line7", "&bGrund &8» &c %grund%");
                settings.set("KickMessage.line8", "");
                settings.set("KickMessage.line9", "&8-------------------------------");

                settings.set("BanMessage.lines", 14);
                settings.set("BanMessage.line1", "&8-------------------------------");
                settings.set("BanMessage.line2", "");
                settings.set("BanMessage.line3", "&3&lMegaCraft &e&lNetzwerk&r");
                settings.set("BanMessage.line4", "");
                settings.set("BanMessage.line6", "&4Du wurdest &4&lgebannt!&r");
                settings.set("BanMessage.line7", "");
                settings.set("BanMessage.line9", "&bGrund &8» §c%grund%");
                settings.set("BanMessage.line10", "");
                settings.set("BanMessage.line12", "&2Bis &8» &4%bis%");
                settings.set("BanMessage.line13", "");
                settings.set("BanMessage.line14", "&8-------------------------------");

                settings.set("ReportMessage.lines", 8);
                settings.set("ReportMessage.line1", "&8---------- &aReport &8----------");
                settings.set("ReportMessage.line2", "");
                settings.set("ReportMessage.line3", "&aWer &8- &b%target%");
                settings.set("ReportMessage.line4", "&aVon &8- &b%von%");
                settings.set("ReportMessage.line5", "&aGrund &8- &b%grund%");
                settings.set("ReportMessage.line6", "%teleport%");
                settings.set("ReportMessage.line7", "");
                settings.set("ReportMessage.line8", "&8---------- &aReport &8----------");

                settings.set("WarnMessage.lines", 6);
                settings.set("WarnMessage.line1", "&e[&m===============================&e]");
                settings.set("WarnMessage.line2", "");
                settings.set("WarnMessage.line3", "&c&lWARNUNG! &7(&b%warnCount%&7/&b%maxWarns%&7)");
                settings.set("WarnMessage.line4", "&aGrund: &b%grund%");
                settings.set("WarnMessage.line5", "");
                settings.set("WarnMessage.line6", "&e[&m===============================&e]");

                settings.set("GeoLocate.lines", 6);
                settings.set("GeoLocate.line1", "&8---------- &bGeoLocate &8----------");
                settings.set("GeoLocate.line2", "§aIP§7: §3%IP%");
                settings.set("GeoLocate.line3", "§aStadt§7: §3%STADT%");
                settings.set("GeoLocate.line4", "§aRegion§7: §3%REGION%");
                settings.set("GeoLocate.line5", "§aLand§7: §3%LAND%");
                settings.set("GeoLocate.line6", "&8---------- &bGeoLocate &8----------");




                ConfigurationProvider.getProvider(YamlConfiguration.class).save(settings, settingsFile);
            }
            settings = ConfigurationProvider.getProvider(YamlConfiguration.class).load(settingsFile);
            if (!cooldownsFile.exists() || cooldownsFile == null) {
                cooldownsFile.createNewFile();
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(cooldowns, cooldownsFile);
            }
            cooldowns = ConfigurationProvider.getProvider(YamlConfiguration.class).load(cooldownsFile);
            if (!blacklistFile.exists() || blacklistFile == null) {
                blacklistFile.createNewFile();
                blacklist = ConfigurationProvider.getProvider(YamlConfiguration.class).load(blacklistFile);
                blacklist.set("Blacklist.direkterBan", false);
                blacklist.set("Blacklist.hardMode", false);
                blacklist.set("Blacklist.Words", Arrays.asList("bastard", "fuck", "ficker", "fiker", "hitler", "hurensohn", "fick", "wixxer", "hs", "leicht", "laicht", "ez", "e²", "mühelos", "clap", "clapz", "bot", "bodt", "bod", "bastard", "hitler", "huhrensohn", "fick", "wixxer", "Hure", "Wichser", "Huan", "Hurensohn", "Arsch", "Arschloch", "Aloch", "Ntte", "Nutte", "Arschkriecher", "Nuttensohn", "Kahbar", "Kachbar", "Drogendealer", "Ficker", "Fickfehler", "Fehlgeburt", "wixer", "Milf", "wihcser", "wiechser", "wiehser", "Hundesohn", "Idiot", "Schlampe", "Pipimann", "Muschi", "Mumu", "Vagina", "Blasen", "Blowjob", "Anal", "Sex", "Blowjop", "Pedo", "Pedophil", "Pedopil", "Pedopiel", "Doggy", "Porno", "GAy", "GAylord", "Nazi", "Natzi", "Nazie"));
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(blacklist, blacklistFile);
            }
            blacklist = ConfigurationProvider.getProvider(YamlConfiguration.class).load(blacklistFile);

            plugin.getLogger().info("Configs geladen!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
