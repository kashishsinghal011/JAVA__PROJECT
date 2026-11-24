package com.thapar.sportssphere.exception;

/** Category: anything that stops a tournament registration. */
public class RegistrationException extends SportsException {
    private static final long serialVersionUID = 1L;

    public RegistrationException(String message) { super(message); }
}
