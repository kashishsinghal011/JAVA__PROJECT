package com.thapar.sportssphere.service;

import com.thapar.sportssphere.model.SportsData;

import java.time.Clock;

/**
 * Central application service (facade). Owns the data and wires the services together
 * so the menu talks to one object. The Clock is injectable so deadline and "past booking"
 * rules can be tested with a fixed date.
 */
public class SportsManagementSystem {
    private final SportsData data;
    private final Clock clock;
    private final PlayerTeamService people;
    private final PaymentService payments;
    private final RegistrationService registrations;
    private final BookingService bookings;
    private final EquipmentService equipment;
    private final AnalyticsService analytics;
    private final MatchService matches;
    private final ReportService reports;

    public SportsManagementSystem() { this(new SportsData(), Clock.systemDefaultZone()); }

    public SportsManagementSystem(SportsData data) { this(data, Clock.systemDefaultZone()); }

    public SportsManagementSystem(SportsData data, Clock clock) {
        this.data = data;
        this.clock = clock;
        this.people = new PlayerTeamService(data);
        this.payments = new PaymentService(data);
        this.registrations = new RegistrationService(data, clock, payments);
        this.bookings = new BookingService(data, clock);
        this.equipment = new EquipmentService(data);
        this.analytics = new AnalyticsService(data);
        this.matches = new MatchService(data, bookings, analytics);
        this.reports = new ReportService(data, analytics);
    }

    /** Records a rejected operation for the failed-operations report; never changes business data. */
    public void logFailure(String operation, Exception error) {
        data.getFailureLog().record(operation, error);
    }

    public SportsData getData() { return data; }
    public Clock getClock() { return clock; }
    public PlayerTeamService people() { return people; }
    public PaymentService payments() { return payments; }
    public RegistrationService registrations() { return registrations; }
    public BookingService bookings() { return bookings; }
    public EquipmentService equipment() { return equipment; }
    public AnalyticsService analytics() { return analytics; }
    public MatchService matches() { return matches; }
    public ReportService reports() { return reports; }
}
