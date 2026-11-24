package com.thapar.sportssphere.util;

/** The user typed "cancel" at a prompt; the menu abandons the current action and returns. */
public class OperationCancelledException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public OperationCancelledException() { super("Cancelled by user."); }
}
