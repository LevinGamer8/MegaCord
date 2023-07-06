package de.megacord.utils;

import de.megacord.MegaCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.megacord.utils.Config.settings;

public class Onlinezeit {

    private java.util.UUID UUID;
    private String name;
    private String datum;
    private CommandSender sender;

    private DataSource source;

    public Onlinezeit(java.util.UUID UUID, String datum, DataSource source) {
        this.UUID = UUID;
        this.datum = datum;
        this.source = source;
    }

    public Onlinezeit(CommandSender sender, DataSource source) {
        this.sender = sender;
        this.source = source;
    }

    public DataSource getSource() {
        return source;
    }

    public void createNew(String name) {
        this.name = name;
        if (wasOnlineToday()) {
            long timeTodayOnline = 0L;
            try (Connection conn = getSource().getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT Datum,Onlinezeit FROM onlinetime WHERE UUID = ? AND DATUM = ? LIMIT 1");) {
                ps.setString(1, this.getUUID().toString());
                ps.setString(2, this.getDatum());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    timeTodayOnline = rs.getLong("onlinezeit");
                }
            } catch (SQLException e) {
                MegaCord.logger().log(Level.WARNING, "could not create player for onlinetime", e);
                return;
            }

            MegaCord.getInstance().getAllOnlineTimeToday().put(this.getUUID(), System.currentTimeMillis() - timeTodayOnline);
        } else {
            MegaCord.getInstance().getAllOnlineTimeToday().put(this.getUUID(), System.currentTimeMillis());
            try (Connection conn = getSource().getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO onlinetime(UUID, Name, Datum, onlinezeit) VALUES (?,?,?,?)");) {
                ps.setString(1, this.getUUID().toString());
                ps.setString(2, this.getName());
                ps.setString(3, this.getDatum());
                ps.setLong(4, 0L);
                ps.executeUpdate();
            } catch (SQLException e) {
                MegaCord.logger().log(Level.WARNING, "failed to insert player into database to create new onlinetime", e);
            }
        }
    }

    private boolean wasOnlineToday() {
        try (Connection conn = getSource().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT UUID,Datum FROM onlinetime WHERE UUID = ? AND Datum = ? LIMIT 1;")) {
            ps.setString(1, this.getUUID().toString());
            ps.setString(2, this.getDatum());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            MegaCord.logger().log(Level.WARNING, "could not check if player was online today", e);
        }
        return false;
    }

    public void leave() {
        if (MegaCord.getInstance().getAllOnlineTimeToday().containsKey(this.getUUID())) {
            long joinTS = MegaCord.getInstance().getAllOnlineTimeToday().get(this.getUUID());
            long leaveTS = System.currentTimeMillis();
            long timeOnline = leaveTS - joinTS;
            MegaCord.getInstance().getAllOnlineTimeToday().remove(this.getUUID());
            try (Connection conn = getSource().getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE onlinetime SET onlinezeit = ? WHERE UUID = ? AND Datum = ?")) {
                ps.setLong(1, timeOnline);
                ps.setString(2, this.getUUID().toString());
                ps.setString(3, this.getDatum());
                ps.executeUpdate();
            } catch (SQLException e) {
                MegaCord.logger().log(Level.WARNING, "failed to update onlinetime if player leave", e);
            }
        }
    }

    private HashMap<String, Long> getLastXDays(ArrayList<String> dayStrings, java.util.UUID from) {
        HashMap<String, Long> times = new HashMap<>();
        LinkedHashMap<String, Long> zeiten = new LinkedHashMap<>();
        String startSQL = "SELECT * FROM onlinetime WHERE UUID=? AND (Datum=?";
        StringBuilder dates = new StringBuilder();
        for (int i = 1; i < dayStrings.size(); i++) {
            dates.append(" OR Datum=?");
        }
        String finishSQL = startSQL + dates + ")";
        try (Connection conn = getSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(finishSQL)) {
            ps.setString(1, from.toString());
            for (int i = 2; i <= dayStrings.size() + 1; i++) {
                ps.setString(i, dayStrings.get(i - 2));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                times.put(rs.getString("Datum"), rs.getLong("onlinezeit"));
            }
        } catch (SQLException e) {
            MegaCord.logger().log(Level.WARNING, "could not get lastXdays", e);
        }
        for (String dayString : dayStrings) {
            zeiten.put(dayString, times.getOrDefault(dayString, 0L));
        }
        return zeiten;
    }

    public void sendTrend(UUID from, int lastDays, boolean consoleConform) {
        sendTrend(from, lastDays, consoleConform, "");
    }

    public void sendTrend(UUID from, int lastDays, boolean consoleConform, String text) {
        double yQuer, tQuer, b, tyt = 0, T, t2 = 0, tQuer2, a, mQuer;

        ArrayList<String> dayStrings = new ArrayList<>();
        HashMap<Integer, Long> onlineInMS = new HashMap<>();
        long prognose;
        long sumX = 0L;
        long sumY = 0L;
        long currentTS = System.currentTimeMillis();
        long aDay = 86400000L;
        // mein erstes element ist in der tabelle 1
        int counter = lastDays;
        for (int i = 1; i <= lastDays; i++) {
            long dayBefore = currentTS - (aDay * (counter - 1)); // HEUTE WIRD MITGEZÄHLT!
            LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(dayBefore), ZoneId.of("Europe/Berlin"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dayBeforeString = date.format(formatter);
            dayStrings.add(dayBeforeString);
            counter--;
        }

        ArrayList<Long> perDay = new ArrayList<>(getLastXDays(dayStrings, from).values());
        Collections.reverse(perDay);
        counter = lastDays;
        for (int i = 1; i <= lastDays; i++) {
            onlineInMS.put(i, perDay.get(counter - 1));
            sumY += perDay.get(counter - 1);
            sumX += i;
            tyt = tyt + (i * perDay.get(counter - 1));
            t2 = t2 + Math.pow(i, 2);
            counter--;
        }

        yQuer = (double) sumY / perDay.size();
        tQuer = (double) sumX / lastDays;
        T = lastDays;
        tQuer2 = Math.pow(tQuer, 2);
        b = (tyt - T * yQuer * tQuer) / (t2 - T * tQuer2);
        a = yQuer - b * tQuer;
        mQuer = a + b * (lastDays + 1);
        prognose = Math.round(mQuer);
        if (a == 0) {
            this.getSender().sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + "Dieser Spieler war in den letzten " + MegaCord.herH + lastDays + MegaCord.fehler + " Tagen nicht Online!"));
            return;
        }
        TextComponent tc = new TextComponent();
        if (!text.equalsIgnoreCase(""))
            tc.setText(MegaCord.Prefix + text);
        else
            tc.setText(MegaCord.Prefix + "Hier der Trend von " + UUIDFetcher.getName(from) + ": ");
        TextComponent tc1 = new TextComponent();
        tc1.setText(MegaCord.other2 + "[" + MegaCord.fehler + "MEHR" + MegaCord.other2 + "]");

    }

    public void sendWeek(String fromUUID, boolean consoleConform) {
        if (1 > 0)
            return;
        // consoleConform steht dafür, ob es denn für die Console extra ausgegeben werden soll (wegen Textcomponent usw.)
        long currentTS = System.currentTimeMillis();
        long aDay = 86400000L;
        LinkedHashMap<String, Long> days = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            long dayBefore = currentTS - (aDay * (i)); // HEUTE WIRD MITGEZÄHLT!
            LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(dayBefore), ZoneId.of("Europe/Berlin"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter niceFormatter = DateTimeFormatter.ofPattern("EEEE");
            String dayBeforeString = date.format(formatter);
            if (i == 0) {
                long actualTime = getActualTime(java.util.UUID.fromString(fromUUID));
                if (actualTime != -1)
                    days.put(date.format(niceFormatter), actualTime);
                else
                    days.put(date.format(niceFormatter), getMsPerDay(dayBeforeString, fromUUID));
            } else
                days.put(date.format(niceFormatter), getMsPerDay(dayBeforeString, fromUUID));
        }
        String today = "Heute";
        int count = 0;
        TextComponent finalTC = new TextComponent();
        ArrayList<String> hover = new ArrayList<>();
        for (String date : days.keySet()) {
            count++;
            String time = getTimeString(days.get(date), false);
            String zeichen = "§c✘";
            if (consoleConform)
                zeichen = "§cX";
            hover.add(MegaCord.Prefix + today + " §f| §7" + date + " §f » §e" + (days.get(date) < 1000 ? zeichen : time));
            if (count == 1) {
                today = "Gestern";
            } else {
                today = "...";
            }
            if (date.equals("Montag")) {
                today = "letzte Woche";
            }
        }
        if (consoleConform) {
            MegaCord.logger().info(String.join("\n", hover));
        } else {
            TextComponent tc = new TextComponent();
            tc.setText(MegaCord.Prefix + "Hier eine Übersicht über die vergangenen §b7 §aTage: ");
            finalTC.setText(MegaCord.other2 + "[" + MegaCord.fehler + "MEHR" + MegaCord.other2 + "]");
            finalTC.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(String.join("\n", hover))));
            tc.addExtra(finalTC);
            this.getSender().sendMessage(tc);
        }
    }

    public void sendFromDate(Long dateTS, String player) {
        try (Connection conn = getSource().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM onlinetime WHERE " + (dateTS == null ? "" : "Datum = ?") + (player != null && dateTS != null ? " AND " : "") + (player == null ? "" : "UUID = ?"))) {
            LocalDateTime date = null;
            DateTimeFormatter formatter = null;
            if (dateTS != null) {
                date = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTS), ZoneId.of("Europe/Berlin"));
                formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                ps.setString(1, date.format(formatter));
            }
            if (player != null)
                ps.setString((dateTS == null ? 1 : 2), UUIDFetcher.getUUID(player).toString());
            ResultSet rs = ps.executeQuery();
            int counter = 0;
            this.getSender().sendMessage(new TextComponent(MegaCord.Prefix + "Tag: " + MegaCord.herH + (dateTS == null ? "Insgesamt" : date.format(formatter)) + MegaCord.other2 + " (" + MegaCord.herH + (player == null ? "Alle" : player) + MegaCord.other2 + ")"));

            String message = settings.getString("Onlinezeit");

            if (message.startsWith("%onlinezeit%")) {
                message = "uiofzp" + message;
            }
            if (message.endsWith("%onlinezeit%")) {
                message = message + "uiofzp";
            }

            String[] split = message.split("%onlinezeit%");

            long allTime = 0;
            TextComponent finalTC = new TextComponent();
            while (rs.next()) {
                finalTC = new TextComponent();
                if (dateTS == null) {
                    allTime = allTime + rs.getLong("onlinezeit");
                }
                for (int i = 0; i < split.length; i++) {
                    TextComponent tc = new TextComponent();
                    if (i == 1) { // %onlinezeit% Textcomponent
                        TextComponent tc1 = new TextComponent();
                        if (MegaCord.getInstance().getAllOnlineTimeToday().containsKey(java.util.UUID.fromString(rs.getString("UUID")))) {
                            // Ist der aktuelle Spieler online?
                            long dif = getActualTime(UUIDFetcher.getUUID(rs.getString("Name")));
                            if (dateTS != null) {
                                LocalDateTime date2 = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Europe/Berlin"));
                                DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                                if (date.format(formatter).equalsIgnoreCase(date2.format(formatter2))) {
                                    // heutiger Tag
                                    tc1 = calcTime((allTime + dif), (!(this.getSender() instanceof ProxiedPlayer))); // tt
                                } else
                                    tc1 = calcTime(rs.getLong("onlinezeit"), (!(this.getSender() instanceof ProxiedPlayer)));
                            } else {
                                tc1 = calcTime(allTime, (!(this.getSender() instanceof ProxiedPlayer)));
                            }
                        } else {
                            // PERSON NICHT ONLINE
                            long time = allTime;
                            if (dateTS != null)
                                time = time + rs.getLong("onlinezeit");
                            tc1 = calcTime(time, (!(this.getSender() instanceof ProxiedPlayer)));
                        }
                        tc1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/check " + rs.getString("Name")));
                        finalTC.addExtra(tc1);
                    }
                    tc.setText(ChatColor.translateAlternateColorCodes('&', split[i].replace("%player%", (player == null ? rs.getString("Name") : "")).replace("uiofzp", "")));
                    finalTC.addExtra(tc);
                }
                if (dateTS != null)
                    this.getSender().sendMessage(finalTC);
                counter++;
            }
            if (dateTS == null) {
                if (!(counter <= 0))
                    this.getSender().sendMessage(finalTC);
            }
            if (counter <= 0)
                this.getSender().sendMessage(new TextComponent(MegaCord.Prefix + MegaCord.fehler + (player == null ? "Niemand war Online :(" : "Der Spieler war nicht Online :(")));
        } catch (SQLException e) {
            MegaCord.logger().log(Level.WARNING, "could not get onlinezeit", e);
        }
    }

    private long getMsPerDay(String day, String fromUUID) {
        try (Connection conn = getSource().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT onlinezeit FROM onlinetime WHERE UUID = ? AND Datum = ?")) {
            ps.setString(1, fromUUID);
            ps.setString(2, day);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                return rs.getLong("onlinezeit");
            }
            return 0L;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private boolean checkString(String toCheck) {
        final String regex = "^(0\\d|1\\d|2\\d|3[0-1])\\/(0[1-9]|1[0-2])\\/(20\\d\\d)";

        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(toCheck);
        return matcher.find();
    }

    private TextComponent calcTime(long time, boolean console) {
        long sec = time / 1000;
        if (time < 1000 && time > 0)
            sec = -1;
        long min = 0;
        long hour = 0;
        long day = 0;
        long week = 0;

        while (sec >= 60) {
            sec -= 60;
            min++;
        }

        while (min >= 60) {
            min -= 60;
            hour++;
        }

        while (hour >= 24) {
            hour -= 24;
            day++;
        }

        while (day >= 7) {
            day -= 7;
            week++;
        }
        TextComponent tc = new TextComponent();
        tc.setText(MegaCord.other2 + "[" + MegaCord.fehler + "MEHR" + MegaCord.other2 + "]");
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MegaCord.herH + (week == 0 ? "" : week + " Woche(n), ") + (day == 0 ? "" : day + " Tag(e), ") + (hour == 0 ? "" : hour + " Stunde(n), ") + (min == 0 ? "" : min + " Minute(n), ") + (sec == 0 ? "" : sec + " Sekunde(n)"))));
        if (console) {
            TextComponent tc1 = new TextComponent();
            tc1.setText(MegaCord.herH + (week == 0 ? "" : week + " Woche(n), ") + (day == 0 ? "" : day + " Tag(e), ") + (hour == 0 ? "" : hour + " Stunde(n), ") + (min == 0 ? "" : min + " Minute(n), ") + (sec == 0 ? "" : sec == -1 ? "<1" : sec + " Sekunde(n)"));
            return tc1;
        }
        return tc;
    }

    private String getTimeString(long time, boolean plus) {
        boolean minus = false;
        if (time < 0) {
            minus = true;
            time = time / (-1);
        }
        long sec = time / 1000;
        if (time < 1000 && time > 0)
            sec = -1;
        long min = 0;
        long hour = 0;
        long day = 0;
        long week = 0;

        while (sec >= 60) {
            sec -= 60;
            min++;
        }

        while (min >= 60) {
            min -= 60;
            hour++;
        }

        while (hour >= 24) {
            hour -= 24;
            day++;
        }

        while (day >= 7) {
            day -= 7;
            week++;
        }
        return MegaCord.herH + (minus ? "-" : plus ? "+" : "") + (week == 0 ? "" : week + " Woche(n), ") + (day == 0 ? "" : day + " Tag(e), ") + (hour == 0 ? "" : hour + " Stunde(n), ") + (min == 0 ? "" : min + " Minute(n), ") + (sec == 0 ? "" : sec == -1 ? "<1" : sec + " Sekunde(n)");
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public long getActualTime(java.util.UUID fromUUID) {
        try {
            if (ProxyServer.getInstance().getPlayer(fromUUID).isConnected()) {
                long joinTS = MegaCord.getInstance().getAllOnlineTimeToday().get(fromUUID);
                long leaveTS = System.currentTimeMillis();
                return leaveTS - joinTS;
            } else {
                return -1;
            }
        } catch (NullPointerException e) {
            return -1;
        }
    }

    public void sendTop(boolean week, boolean console) {
        if (week) {
            // per Woche
            this.getSender().sendMessage(new TextComponent(MegaCord.Prefix + "Top" + MegaCord.other2 + " - " + MegaCord.herH + "Woche"));
            ArrayList<String> validDates = new ArrayList<>();

            long currentTS = System.currentTimeMillis();
            long aDay = 86400000L;
            for (int i = 0; i < 7; i++) {
                long dayBefore = currentTS - (aDay * (i)); // HEUTE WIRD MITGEZÄHLT!
                LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(dayBefore), ZoneId.of("Europe/Berlin"));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String dayBeforeString = date.format(formatter);
                validDates.add(dayBeforeString);
            }

            Map<String, Long> nameToTime = new HashMap<>();
            try (Connection conn = getSource().getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT onlinezeit,name,datum FROM onlinetime")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String date = rs.getString("datum");
                    long onlinezeit = rs.getLong("onlinezeit");
                    String name = rs.getString("name");
                    if (validDates.contains(date)) {
                        if (nameToTime.containsKey(name)) {
                            long oldtime = nameToTime.get(name);
                            oldtime += onlinezeit;
                            nameToTime.remove(name);
                            nameToTime.put(name, oldtime);
                        } else
                            nameToTime.put(name, onlinezeit);
                    }
                }
                nameToTime = sortByValue(false, nameToTime);
            } catch (SQLException e) {
                MegaCord.logger().log(Level.WARNING, "failed to get data per week", e);
            }
            int count = 0;
            try {
                for (int i = 0; i < 3; i++) {
                    TextComponent tc = new TextComponent();
                    tc.setText(MegaCord.Prefix + MegaCord.herH + "#" + (i + 1) + MegaCord.normal + " " + nameToTime.keySet().toArray()[i] + " §f» ");
                    tc.addExtra(calcTime(nameToTime.get(nameToTime.keySet().toArray()[i]), console));
                    this.getSender().sendMessage(tc);
                    count++;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                this.getSender().sendMessage(new TextComponent(MegaCord.Prefix + "Es war(en) diese Woche nur " + MegaCord.herH + count + MegaCord.normal + " Spieler online"));
            }
        } else {
            // per total
            this.getSender().sendMessage(new TextComponent(MegaCord.Prefix + "Top" + MegaCord.other2 + " - " + MegaCord.herH + "Insgesamt"));
            Map<String, Long> nameToTime = new HashMap<>();
            try (Connection conn = getSource().getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT onlinezeit,name FROM onlinetime")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String name = rs.getString("Name");
                    long onlinezeit = rs.getLong("onlinezeit");
                    if (nameToTime.containsKey(name)) {
                        long oldtime = nameToTime.get(name);
                        oldtime += onlinezeit;
                        nameToTime.remove(name);
                        nameToTime.put(name, oldtime);
                    } else
                        nameToTime.put(name, onlinezeit);
                }
            } catch (SQLException e) {
                MegaCord.logger().log(Level.WARNING, "failed to get data per total");
            }
            nameToTime = sortByValue(false, nameToTime);
            int count = 3;
            if (nameToTime.size() < 3)
                count = nameToTime.size();
            for (int i = 0; i < count; i++) {
                TextComponent tc = new TextComponent();
                tc.setText(MegaCord.Prefix + MegaCord.herH + "#" + (i + 1) + MegaCord.normal + " " + nameToTime.keySet().toArray()[i] + " §f» ");
                tc.addExtra(calcTime(nameToTime.get(nameToTime.keySet().toArray()[i]), console));
                this.getSender().sendMessage(tc);
            }
        }
    }

    private Map<String, Long> sortByValue(boolean order, Map<String, Long> map) {
        //convert HashMap into List
        List<Map.Entry<String, Long>> list = new LinkedList<Map.Entry<String, Long>>(map.entrySet());
        //sorting the list elements
        Collections.sort(list, new Comparator<Map.Entry<String, Long>>() {
            public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                if (order) {
                    //compare two object and return an integer
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());
                }
            }
        });
        //prints the sorted HashMap
        Map<String, Long> sortedMap = new LinkedHashMap<String, Long>();
        for (Map.Entry<String, Long> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    public CommandSender getSender() {
        return sender;
    }

    public java.util.UUID getUUID() {
        return UUID;
    }

    public String getName() {
        return name;
    }

    public String getDatum() {
        return datum;
    }
}
