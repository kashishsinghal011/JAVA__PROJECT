package com.thapar.sportssphere.util;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keeps a bounded history of failed operations for the "failed operations" report.
 * Recording a failure never changes business data; it only documents what was rejected and why.
 */
public class FailureLog implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MAX_ENTRIES = 500;

    public record Entry(LocalDateTime time, String operation, String exceptionType, String message)
            implements Serializable { }

    private final List<Entry> entries = new ArrayList<>();

    public void record(String operation, Throwable error) {
        if (entries.size() >= MAX_ENTRIES) {
            entries.remove(0);      // drop the oldest; the log must not grow without limit
        }
        entries.add(new Entry(LocalDateTime.now(), operation, error.getClass().getSimpleName(), error.getMessage()));
    }

    public List<Entry> getEntries() { return Collections.unmodifiableList(entries); }

    public int size() { return entries.size(); }

    public String generateReport() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MMM HH:mm:ss");
        StringBuilder sb = new StringBuilder("FAILED OPERATIONS\n");
        if (entries.isEmpty()) {
            sb.append("   (none recorded)\n");
        }
        int n = 1;
        for (Entry e : entries) {
            sb.append(String.format("%3d. %s | %-28s | %s%n       Reason: %s%n",
                    n++, e.time().format(fmt), e.exceptionType(), e.operation(), e.message()));
        }
        return sb.toString();
    }
}
