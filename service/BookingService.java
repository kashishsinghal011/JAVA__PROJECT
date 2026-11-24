package com.thapar.sportssphere.service;

import com.thapar.sportssphere.exception.FacilityUnavailableException;
import com.thapar.sportssphere.exception.InvalidBookingException;
import com.thapar.sportssphere.exception.InvalidOperationException;
import com.thapar.sportssphere.model.FacilityBooking;
import com.thapar.sportssphere.model.SportsData;
import com.thapar.sportssphere.model.SportsFacility;
import com.thapar.sportssphere.model.TimeSlot;
import com.thapar.sportssphere.model.enums.SportType;
import com.thapar.sportssphere.util.IdGenerator;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Facility set-up and the booking engine. Overlap detection itself lives in SportsFacility. */
public class BookingService {
    private final SportsData data;
    private final Clock clock;

    public BookingService(SportsData data, Clock clock) {
        this.data = data;
        this.clock = clock;
    }

    public SportsFacility addFacility(String name, SportType type, LocalTime opensAt, LocalTime closesAt)
            throws InvalidOperationException {
        SportsFacility f = new SportsFacility(IdGenerator.next("FAC"), name, type, opensAt, closesAt);
        data.addFacility(f);
        return f;
    }

    /** Overload: default operating hours. */
    public SportsFacility addFacility(String name, SportType type) throws InvalidOperationException {
        SportsFacility f = new SportsFacility(IdGenerator.next("FAC"), name, type);
        data.addFacility(f);
        return f;
    }

    public FacilityBooking book(String facilityId, LocalDateTime start, LocalDateTime end,
                                String purpose, String bookedBy)
            throws InvalidBookingException, FacilityUnavailableException, InvalidOperationException {
        return book(facilityId, IdGenerator.next("BKG"), start, end, purpose, bookedBy);
    }

    /** Overload with a caller-chosen booking id (used by match scheduling so a match can find its booking). */
    public FacilityBooking book(String facilityId, String bookingId, LocalDateTime start, LocalDateTime end,
                                String purpose, String bookedBy)
            throws InvalidBookingException, FacilityUnavailableException, InvalidOperationException {
        SportsFacility facility = data.findFacility(facilityId);
        FacilityBooking booking = new FacilityBooking(bookingId, facility.getFacilityId(), start, end, purpose, bookedBy);
        if (start.isBefore(LocalDateTime.now(clock))) {
            throw new InvalidBookingException("start time is in the past");
        }
        facility.book(booking);                 // throws FacilityUnavailableException on overlap; list unchanged
        return booking;
    }

    public void cancel(String facilityId, String bookingId) throws InvalidOperationException {
        data.findFacility(facilityId).cancelBooking(bookingId);
    }

    public List<TimeSlot> freeSlots(String facilityId, LocalDate date) throws InvalidOperationException {
        return data.findFacility(facilityId).getFreeSlots(date);
    }
}
