package com.thapar.sportssphere.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Generates readable ids such as PLR-001. observe() re-syncs the counters after loading
 * saved data so new ids never collide with loaded ones.
 */
public final class IdGenerator {
    private static final Map<String, Integer> COUNTERS = new HashMap<>();

    private IdGenerator() { }

    public static synchronized String next(String prefix) {
        int n = COUNTERS.merge(prefix, 1, Integer::sum);
        return String.format("%s-%03d", prefix, n);
    }

    public static synchronized void observe(String id) {
        int dash = id.lastIndexOf('-');
        if (dash <= 0) return;
        try {
            int n = Integer.parseInt(id.substring(dash + 1));
            COUNTERS.merge(id.substring(0, dash), n, Math::max);
        } catch (NumberFormatException ignored) {
            // id does not follow PREFIX-number, so it can never collide with generated ids.
        }
    }

    public static synchronized void reset() { COUNTERS.clear(); }
}
