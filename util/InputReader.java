package com.thapar.sportssphere.util;

import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * All console input goes through here. Every method re-prompts on bad input, always consumes
 * the whole line (so no stuck-token infinite loop), stops cleanly at end of input, and lets the
 * user type "cancel" to abandon an action.
 */
public class InputReader {
    private final Scanner scanner;
    private final PrintStream out;

    public InputReader(InputStream in, PrintStream out) {
        this.scanner = new Scanner(in);
        this.out = out;
    }

    private String rawLine(String prompt) {
        out.print(prompt + ": ");
        out.flush();
        if (!scanner.hasNextLine()) {
            throw new InputClosedException();
        }
        String line = scanner.nextLine().trim();
        if (line.equalsIgnoreCase("cancel")) {
            throw new OperationCancelledException();
        }
        return line;
    }

    /** Text that may be left blank. */
    public String readOptional(String prompt) { return rawLine(prompt); }

    /** Text that must not be empty. */
    public String readRequired(String prompt) {
        while (true) {
            String s = rawLine(prompt);
            if (!s.isEmpty()) return s;
            out.println("  Please enter a value (or type 'cancel').");
        }
    }

    /** Any whole number; business limits (e.g. a valid age or score) are enforced by the model. */
    public int readInt(String prompt) { return readInt(prompt, Integer.MIN_VALUE, Integer.MAX_VALUE); }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            String s = rawLine(prompt);
            try {
                int v = Integer.parseInt(s);
                if (v >= min && v <= max) return v;
                out.println("  Enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                out.println("  '" + s + "' is not a whole number.");
            }
        }
    }

    public double readDouble(String prompt, double min, double max) {
        while (true) {
            String s = rawLine(prompt);
            try {
                double v = Double.parseDouble(s);
                if (!Double.isNaN(v) && v >= min && v <= max) return v;
                out.println("  Enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                out.println("  '" + s + "' is not a number.");
            }
        }
    }

    public LocalDate readDate(String prompt) {
        while (true) {
            String s = rawLine(prompt + " (" + DateTimeUtil.DATE_HINT + ")");
            try {
                return LocalDate.parse(s, DateTimeUtil.DATE);
            } catch (DateTimeParseException e) {
                out.println("  '" + s + "' is not a valid date. Example: 25-12-2026");
            }
        }
    }

    public LocalTime readTime(String prompt) {
        while (true) {
            String s = rawLine(prompt + " (" + DateTimeUtil.TIME_HINT + ")");
            try {
                return LocalTime.parse(s, DateTimeUtil.TIME);
            } catch (DateTimeParseException e) {
                out.println("  '" + s + "' is not a valid time. Example: 16:30");
            }
        }
    }

    public boolean readYesNo(String prompt) {
        while (true) {
            String s = rawLine(prompt + " (y/n)").toLowerCase();
            if (s.equals("y") || s.equals("yes")) return true;
            if (s.equals("n") || s.equals("no")) return false;
            out.println("  Please answer y or n.");
        }
    }

    /** Shows a numbered list of the enum constants and returns the chosen one. */
    public <E extends Enum<E>> E readEnum(String prompt, E[] values) {
        out.println(prompt + ":");
        for (int i = 0; i < values.length; i++) {
            out.println("   " + (i + 1) + ". " + values[i]);
        }
        int choice = readInt("Choose 1-" + values.length, 1, values.length);
        return values[choice - 1];
    }
}
