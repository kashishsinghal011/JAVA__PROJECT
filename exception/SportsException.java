package com.thapar.sportssphere.exception;

/**
 * Root of every business-rule failure in SportsSphere. Checked on purpose: the caller
 * must decide how to recover (reject the operation, ask again, roll back).
 */
public class SportsException extends Exception {
    private static final long serialVersionUID = 1L;

    public SportsException(String message) { super(message); }

    public SportsException(String message, Throwable cause) { super(message, cause); }
}
