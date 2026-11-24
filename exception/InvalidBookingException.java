package com.thapar.sportssphere.exception;

/** Booking interval or purpose breaks the booking rules. */
public class InvalidBookingException extends BookingException {
    private static final long serialVersionUID = 1L;

    public InvalidBookingException(String reason) {
        super("Invalid booking: " + reason);
    }
}
