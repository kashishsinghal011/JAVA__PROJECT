package com.thapar.sportssphere.exception;

/** Registration limit of the tournament reached. */
public class TournamentFullException extends RegistrationException {
    private static final long serialVersionUID = 1L;

    public TournamentFullException(String tournamentName, int limit) {
        super("Tournament " + tournamentName + " is full (limit " + limit + " entries).");
    }
}
