package com.thapar.sportssphere.model;

import com.thapar.sportssphere.exception.InvalidOperationException;
import com.thapar.sportssphere.model.enums.RegistrationStatus;
import com.thapar.sportssphere.model.enums.PaymentStatus;
import com.thapar.sportssphere.util.Validator;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Links ONE participant (an individual player OR a whole team) to a tournament.
 * COMPOSITION: owns its Payment. Created through factories so exactly one of player/team is set.
 */
public final class Registration implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String registrationId;
    private final Player player;      // null for team registrations
    private final Team team;          // null for individual registrations
    private final String tournamentId;
    private final LocalDate registeredOn;
    private RegistrationStatus status = RegistrationStatus.PENDING;
    private final Payment payment;

    private Registration(String id, Player player, Team team, String tournamentId, double fee, LocalDate on) {
        this.registrationId = Validator.requireNonBlank(id, "Registration id");
        this.player = player;
        this.team = team;
        this.tournamentId = Validator.requireNonBlank(tournamentId, "Tournament id");
        this.registeredOn = Validator.requireNonNull(on, "Registration date");
        this.payment = new Payment("PAY-" + id, fee);
    }

    public static Registration forPlayer(String id, Player player, String tournamentId, double fee, LocalDate on) {
        return new Registration(id, Validator.requireNonNull(player, "Player"), null, tournamentId, fee, on);
    }

    public static Registration forTeam(String id, Team team, String tournamentId, double fee, LocalDate on) {
        return new Registration(id, null, Validator.requireNonNull(team, "Team"), tournamentId, fee, on);
    }

    public boolean isTeamEntry() { return team != null; }

    public String getParticipantName() { return team != null ? team.getName() : player.getName(); }

    /** Ids of every player this entry puts into the tournament; used for duplicate detection. */
    public Set<String> coveredPlayerIds() {
        Set<String> ids = new HashSet<>();
        if (player != null) {
            ids.add(player.getId());
        } else {
            for (Player p : team.getRoster()) ids.add(p.getId());
        }
        return ids;
    }

    public boolean isActive() { return status != RegistrationStatus.CANCELLED; }

    /** A registration may only be confirmed after its payment succeeded. */
    public void confirm() throws InvalidOperationException {
        if (status != RegistrationStatus.PENDING) {
            throw new InvalidOperationException("registration " + registrationId + " is " + status + ", not PENDING");
        }
        if (!payment.isSuccessful()) {
            throw new InvalidOperationException("registration " + registrationId + " cannot be confirmed before payment succeeds");
        }
        status = RegistrationStatus.CONFIRMED;
    }

    public void cancel() { status = RegistrationStatus.CANCELLED; }

    public String getRegistrationId() { return registrationId; }
    public Player getPlayer() { return player; }
    public Team getTeam() { return team; }
    public String getTournamentId() { return tournamentId; }
    public LocalDate getRegisteredOn() { return registeredOn; }
    public RegistrationStatus getStatus() { return status; }
    public Payment getPayment() { return payment; }

    /**
     * Persistence support: applies the saved status and payment state of this registration
     * while loading data, bypassing the live state machine (which only moves PENDING forward
     * under today's rules). The saved state was created by the domain itself.
     */
    public void restore(RegistrationStatus savedStatus, PaymentStatus paymentStatus,
                        double paymentAmount, java.time.LocalDateTime paymentProcessedAt) {
        Validator.requireNonNull(savedStatus, "Saved registration status");
        Validator.requireNonNull(paymentStatus, "Saved payment status");
        this.status = savedStatus;
        payment.restore(paymentAmount, paymentStatus, paymentProcessedAt);
    }

    @Override
    public String toString() {
        return String.format("%s %-6s %-20s %-10s %s", registrationId, isTeamEntry() ? "TEAM" : "PLAYER",
                getParticipantName(), status, payment);
    }
}
