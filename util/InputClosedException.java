package com.thapar.sportssphere.util;

/** Standard input ended (Ctrl+D / Ctrl+Z or piped input finished). Lets the menu exit instead of looping forever. */
public class InputClosedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InputClosedException() { super("Input closed."); }
}
