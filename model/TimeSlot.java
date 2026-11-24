package com.thapar.sportssphere.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Immutable free or booked time interval. A record fits: pure data, no behaviour to override. */
public record TimeSlot(LocalDateTime start, LocalDateTime end) implements Serializable {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");

    public long minutes() { return Duration.between(start, end).toMinutes(); }

    @Override
    public String toString() { return start.format(FMT) + " - " + end.format(FMT); }
}
