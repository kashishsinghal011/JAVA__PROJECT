package com.thapar.sportssphere.exception;

/** Lookup of a player that does not exist. */
public class PlayerNotFoundException extends SportsException {
    private static final long serialVersionUID = 1L;

    public PlayerNotFoundException(String playerId) {
        super("No player found with id " + playerId + ".");
    }
}
