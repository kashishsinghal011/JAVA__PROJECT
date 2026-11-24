package com.thapar.sportssphere.model.enums;

/** Lifecycle of a tournament; transitions are enforced inside Tournament. */
public enum TournamentStatus {
    DRAFT, REGISTRATION_OPEN, REGISTRATION_CLOSED, IN_PROGRESS, COMPLETED
}
