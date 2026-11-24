package com.thapar.sportssphere.util;

import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

/** One place for date/time formats. STRICT resolving rejects impossible dates such as 31-02-2026. */
public final class DateTimeUtil {
    private DateTimeUtil() { }

    public static final String DATE_HINT = "dd-MM-yyyy";
    public static final String TIME_HINT = "HH:mm, 24-hour";

    public static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);
}
