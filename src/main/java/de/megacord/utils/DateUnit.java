package de.megacord.utils;

public enum DateUnit {

    MIN(60),
    HOUR(60*60),
    DAY(24*60*60),
    WEEK(7*24*60*60),
    MON(30*24*60*60),
    YEAR(365*24*60*60);

    private long toSec;

    private DateUnit(long toSec) {
        this.toSec = toSec;
    }

    public long getToSec() {
        return toSec;
    }
}
