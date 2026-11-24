package com.thapar.sportssphere.model;

import com.thapar.sportssphere.exception.InvalidBookingException;
import com.thapar.sportssphere.model.enums.BookingStatus;
import com.thapar.sportssphere.util.Validator;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** One reservation of a facility. Interval rules are enforced in the constructor. */
public class FacilityBooking implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MIN_MINUTES = 30;
    public static final int MAX_MINUTES = 240;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    private final String bookingId;
    private final String facilityId;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final String purpose;
    private final String bookedBy;
    private BookingStatus status = BookingStatus.CONFIRMED;

    public FacilityBooking(String bookingId, String facilityId, LocalDateTime start, LocalDateTime end,
                           String purpose, String bookedBy) throws InvalidBookingException {
        this.bookingId = Validator.requireNonBlank(bookingId, "Booking id");
        this.facilityId = Validator.requireNonBlank(facilityId, "Facility id");
        if (start == null || end == null) {
            throw new InvalidBookingException("start and end time are required");
        }
        if (!end.isAfter(start)) {
            throw new InvalidBookingException("end time must be after start time");
        }
        if (!start.toLocalDate().equals(end.toLocalDate())) {
            throw new InvalidBookingException("a booking must start and end on the same day");
        }
        long minutes = Duration.between(start, end).toMinutes();
        if (minutes < MIN_MINUTES || minutes > MAX_MINUTES) {
            throw new InvalidBookingException("duration must be between " + MIN_MINUTES + " and "
                    + MAX_MINUTES + " minutes (got " + minutes + ")");
        }
        this.start = start;
        this.end = end;
        this.purpose = Validator.requireNonBlank(purpose, "Purpose");
        this.bookedBy = Validator.requireNonBlank(bookedBy, "Booked by");
    }

    /** Half-open interval test: 10:00-11:00 and 11:00-12:00 do NOT overlap. */
    public boolean overlaps(FacilityBooking other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    /** Overload for checking a raw interval before a booking object exists. */
    public boolean overlaps(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return start.isBefore(otherEnd) && otherStart.isBefore(end);
    }

    public boolean isConfirmed() { return status == BookingStatus.CONFIRMED; }

    public void cancel() { this.status = BookingStatus.CANCELLED; }

    public String describe() {
        return bookingId + " (" + purpose + ", " + start.format(FMT) + " to " + end.format(DateTimeFormatter.ofPattern("HH:mm")) + ")";
    }

    public String getBookingId() { return bookingId; }
    public String getFacilityId() { return facilityId; }
    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
    public String getPurpose() { return purpose; }
    public String getBookedBy() { return bookedBy; }
    public BookingStatus getStatus() { return status; }

    @Override
    public String toString() { return describe() + " [" + status + "]"; }
}
