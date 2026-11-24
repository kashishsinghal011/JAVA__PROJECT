package com.thapar.sportssphere.exception;

/** Score is negative, absurdly large, or a draw in a sport that forbids draws. */
public class InvalidScoreException extends SportsException {
    private static final long serialVersionUID = 1L;

    public InvalidScoreException(String reason) {
        super("Invalid score: " + reason);
    }
}
