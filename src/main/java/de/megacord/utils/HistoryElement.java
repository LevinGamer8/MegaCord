package de.megacord.utils;


public class HistoryElement {

    private String targetName;
    private String vonName;
    private String type;
    private String grund;
    private long erstellt;
    private long bis;
    private int perma;
    private int ban;
    private String vonEntbannt;

    public HistoryElement(String targetName, String vonName, String type, String grund, long erstellt, long bis, int perma, int ban, String vonEntbannt) {
        this.targetName = targetName;
        this.vonName = vonName;
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

    public String getTargetName() {
        return targetName;
    }

    public String getVonName() {
        return vonName;
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
