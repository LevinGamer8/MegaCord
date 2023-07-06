package de.megacord.utils;

import java.util.UUID;

public class HistoryElement {

    private UUID targetUUID;
    private UUID vonUUID;
    private String type;
    private String grund;
    private long erstellt;
    private long bis;
    private int perma;
    private int ban;
    private String vonEntbannt;

    public HistoryElement(UUID targetUUID, UUID vonUUID, String type, String grund, long erstellt, long bis, int perma, int ban, String vonEntbannt) {
        this.targetUUID = targetUUID;
        this.vonUUID = vonUUID;
        this.type = type;
        this.grund = grund;
        this.erstellt = erstellt;
        this.bis = bis;
        this.perma = perma;
        this.ban = ban;
        this.vonEntbannt = vonEntbannt;
    }

    public String getVonEntbannt() {
        return vonEntbannt;
    }

    public UUID getTargetUUID() {
        return targetUUID;
    }

    public UUID getVonUUID() {
        return vonUUID;
    }

    public String getType() {
        return type;
    }

    public String getGrund() {
        return grund;
    }

    public long getErstellt() {
        return erstellt;
    }

    public long getBis() {
        return bis;
    }

    public int getPerma() {
        return perma;
    }

    public int getBan() {
        return ban;
    }
}
