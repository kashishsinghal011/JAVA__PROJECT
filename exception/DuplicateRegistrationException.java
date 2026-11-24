package com.thapar.sportssphere.exception;

/** Same player (directly or via a team) registered twice for one tournament. */
public class DuplicateRegistrationException extends RegistrationException {
    private static final long serialVersionUID = 1L;

    public DuplicateRegistrationException(String playerName, String tournamentName) {
        super(playerName + " is already registered for " + tournamentName + ".");
    }
}
