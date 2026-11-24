package com.thapar.sportssphere.exception;

/** Player fails age, sport-preference or registration-status rules. */
public class IneligiblePlayerException extends RegistrationException {
    private static final long serialVersionUID = 1L;

    public IneligiblePlayerException(String playerName, String reason) {
        super(playerName + " is not eligible: " + reason);
    }
}
