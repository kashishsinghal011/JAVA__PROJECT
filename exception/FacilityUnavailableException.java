package com.thapar.sportssphere.exception;

/** Requested slot overlaps a confirmed booking. */
public class FacilityUnavailableException extends BookingException {
    private static final long serialVersionUID = 1L;

    public FacilityUnavailableException(String facilityName, String conflict) {
        super(facilityName + " is not available; it overlaps with " + conflict);
    }
}
