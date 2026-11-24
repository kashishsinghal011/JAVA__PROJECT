package com.thapar.sportssphere.exception;

/** Category: anything that stops a facility booking. */
public class BookingException extends SportsException {
    private static final long serialVersionUID = 1L;

    public BookingException(String message) { super(message); }
}
