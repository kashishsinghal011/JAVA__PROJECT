package com.thapar.sportssphere.model;

import com.thapar.sportssphere.exception.FacilityUnavailableException;
import com.thapar.sportssphere.exception.InvalidBookingException;
import com.thapar.sportssphere.exception.InvalidOperationException;
import com.thapar.sportssphere.model.enums.SportType;
import com.thapar.sportssphere.util.Validator;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A ground, court or hall. COMPOSITION: bookings belong to the facility and have no
 * meaning without it. The facility is the single place where overlap is detected.
 */
public class SportsFacility implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String facilityId;
    private final String name;
    private final SportType type;
    private final LocalTime opensAt;
    private final LocalTime closesAt;
    private final List<FacilityBooking> bookings = new ArrayList<>();

    public SportsFacility(String facilityId, String name, SportType type, LocalTime opensAt, LocalTime closesAt) {
        this.facilityId = Validator.requireNonBlank(facilityId, "Facility id");
        this.name = Validator.requireNonBlank(name, "Facility name");
        this.type = Validator.requireNonNull(type, "Sport type");
        this.opensAt = Validator.requireNonNull(opensAt, "Opening time");
        this.closesAt = Validator.requireNonNull(closesAt, "Closing time");
        if (!closesAt.isAfter(opensAt)) {
            throw new IllegalArgumentException("Closing time must be after opening time.");
        }
    }

    /** Overload: default operating hours 06:00-22:00. */
    public SportsFacility(String facilityId, String name, SportType type) {
        this(facilityId, name, type, LocalTime.of(6, 0), LocalTime.of(22, 0));
    }

    /**
     * Adds the booking only if it passes every rule; otherwise the list is unchanged.
     */
    public void book(FacilityBooking booking) throws InvalidBookingException, FacilityUnavailableException {
        Validator.requireNonNull(booking, "Booking");
        if (!booking.getFacilityId().equals(facilityId)) {
            throw new InvalidBookingException("booking belongs to a different facility");
        }
        if (booking.getStart().toLocalTime().isBefore(opensAt) || booking.getEnd().toLocalTime().isAfter(closesAt)) {
            throw new InvalidBookingException(name + " is open only " + opensAt + " to " + closesAt);
        }
        for (FacilityBooking existing : bookings) {
            if (existing.isConfirmed() && existing.overlaps(booking)) {
                throw new FacilityUnavailableException(name, existing.describe());
            }
        }
        bookings.add(booking);
        bookings.sort(Comparator.comparing(FacilityBooking::getStart));
    }

    public void cancelBooking(String bookingId) throws InvalidOperationException {
        for (FacilityBooking b : bookings) {
            if (b.getBookingId().equals(bookingId)) {
                if (!b.isConfirmed()) {
                    throw new InvalidOperationException("booking " + bookingId + " is already cancelled");
                }
                b.cancel();
                return;
            }
        }
        throw new InvalidOperationException("no booking " + bookingId + " at " + name);
    }

    public boolean isAvailable(LocalDateTime start, LocalDateTime end) {
        for (FacilityBooking b : bookings) {
            if (b.isConfirmed() && b.overlaps(start, end)) return false;
        }
        return true;
    }

    /** Free gaps in operating hours on the given date, computed by sweeping sorted bookings. */
    public List<TimeSlot> getFreeSlots(LocalDate date) {
        List<TimeSlot> free = new ArrayList<>();
        LocalDateTime cursor = date.atTime(opensAt);
        LocalDateTime close = date.atTime(closesAt);
        for (FacilityBooking b : bookings) {           // already sorted by start time
            if (!b.isConfirmed() || !b.getStart().toLocalDate().equals(date)) continue;
            if (b.getStart().isAfter(cursor)) {
                free.add(new TimeSlot(cursor, b.getStart()));
            }
            if (b.getEnd().isAfter(cursor)) {
                cursor = b.getEnd();
            }
        }
        if (cursor.isBefore(close)) {
            free.add(new TimeSlot(cursor, close));
        }
        return free;
    }

    public List<FacilityBooking> getBookings() { return Collections.unmodifiableList(bookings); }

    /**
     * Persistence support: re-attaches already-built bookings (with their saved status)
     * while loading saved data. Validates the same facility and rejects duplicates, but
     * skips the live booking rules: those were checked when the booking was originally
     * created, and operating hours may have changed since.
     */
    public void restoreBooking(FacilityBooking booking) throws InvalidOperationException {
        Validator.requireNonNull(booking, "Booking");
        if (!booking.getFacilityId().equals(facilityId)) {
            throw new InvalidOperationException("booking belongs to a different facility");
        }
        for (FacilityBooking existing : bookings) {
            if (existing.getBookingId().equals(booking.getBookingId())) {
                throw new InvalidOperationException("booking " + booking.getBookingId() + " already exists at " + name);
            }
        }
        bookings.add(booking);
        bookings.sort(Comparator.comparing(FacilityBooking::getStart));
    }
    public String getFacilityId() { return facilityId; }
    public String getName() { return name; }
    public SportType getType() { return type; }
    public LocalTime getOpensAt() { return opensAt; }
    public LocalTime getClosesAt() { return closesAt; }

    @Override
    public String toString() { return name + " (" + type + ")"; }
}
