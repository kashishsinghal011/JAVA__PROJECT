package com.thapar.sportssphere.exception;

/** Action is incompatible with the current tournament, match or object state. */
public class InvalidOperationException extends SportsException {
    private static final long serialVersionUID = 1L;

    public InvalidOperationException(String reason) {
        super("Operation not allowed: " + reason);
    }
}
