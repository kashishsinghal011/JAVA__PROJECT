package com.thapar.sportssphere.exception;

/** Roster would exceed the sport's maximum team size. */
public class TeamCapacityExceededException extends SportsException {
    private static final long serialVersionUID = 1L;

    public TeamCapacityExceededException(String teamName, int maxSize) {
        super("Team " + teamName + " is full (maximum " + maxSize + " players).");
    }
}
