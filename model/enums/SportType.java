package com.thapar.sportssphere.model.enums;

/** Sports supported by the system. Display names keep reports readable. */
public enum SportType {
    FOOTBALL("Football"),
    BASKETBALL("Basketball"),
    BADMINTON("Badminton"),
    CRICKET("Cricket"),
    VOLLEYBALL("Volleyball"),
    TABLE_TENNIS("Table Tennis");

    private final String displayName;

    SportType(String displayName) { this.displayName = displayName; }

    public String getDisplayName() { return displayName; }

    @Override public String toString() { return displayName; }
}
