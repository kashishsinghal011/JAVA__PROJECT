package com.thapar.sportssphere.util;

/**
 * Small argument checks shared by model constructors/setters.
 * They throw unchecked IllegalArgumentException because a bad argument here is a caller
 * bug or bad input already caught at the menu; business-rule failures use the checked
 * SportsException hierarchy instead.
 */
public final class Validator {
    private Validator() { }

    public static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be empty.");
        }
        return value.trim();
    }

    public static int requireRange(int value, int min, int max, String field) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(field + " must be between " + min + " and " + max + " (got " + value + ").");
        }
        return value;
    }

    public static int requirePositive(int value, String field) {
        if (value <= 0) {
            throw new IllegalArgumentException(field + " must be positive (got " + value + ").");
        }
        return value;
    }

    public static int requireNonNegative(int value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + " must not be negative (got " + value + ").");
        }
        return value;
    }

    public static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return value;
    }
}
