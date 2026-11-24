package com.thapar.sportssphere.model;

import com.thapar.sportssphere.exception.DuplicateRegistrationException;
import com.thapar.sportssphere.exception.IneligiblePlayerException;
import com.thapar.sportssphere.exception.InvalidOperationException;
import com.thapar.sportssphere.exception.TournamentFullException;
import com.thapar.sportssphere.model.contract.Reportable;
import com.thapar.sportssphere.model.enums.RegistrationStatus;
import com.thapar.sportssphere.model.enums.TournamentStatus;
import com.thapar.sportssphere.util.Validator;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A competition for one sport. COMPOSITION: owns its Registrations.
 * An "entry" is one active registration, either a team or an individual player.
 * The tournament enforces its own rules, so no caller can put it in an invalid state.
 */
public class Tournament implements Serializable, Reportable {
    private static final long serialVersionUID = 1L;

    private final String tournamentId;
    private final String name;
    private final Sport sport;
    private final LocalDate registrationDeadline;
    private final int maxEntries;
    private final double feeMultiplier;
    private TournamentStatus status = TournamentStatus.DRAFT;
    private final List<Registration> registrations = new ArrayList<>();

    public Tournament(String tournamentId, String name, Sport sport, LocalDate registrationDeadline,
                      int maxEntries, double feeMultiplier) {
        this.tournamentId = Validator.requireNonBlank(tournamentId, "Tournament id");
        this.name = Validator.requireNonBlank(name, "Tournament name");
        this.sport = Validator.requireNonNull(sport, "Sport");
        this.registrationDeadline = Validator.requireNonNull(registrationDeadline, "Deadline");
        this.maxEntries = Validator.requireRange(maxEntries, 2, 64, "Maximum entries");
        if (feeMultiplier < 0) {
            throw new IllegalArgumentException("Fee multiplier must not be negative.");
        }
        this.feeMultiplier = feeMultiplier;
    }

    // ---- lifecycle: DRAFT -> REGISTRATION_OPEN -> REGISTRATION_CLOSED -> IN_PROGRESS -> COMPLETED ----

    public void openRegistration() throws InvalidOperationException { move(TournamentStatus.DRAFT, TournamentStatus.REGISTRATION_OPEN); }

    public void closeRegistration() throws InvalidOperationException { move(TournamentStatus.REGISTRATION_OPEN, TournamentStatus.REGISTRATION_CLOSED); }

    public void start() throws InvalidOperationException {
        if (getConfirmedTeams().size() < 2) {
            throw new InvalidOperationException("at least 2 confirmed team entries are needed to start " + name);
        }
        move(TournamentStatus.REGISTRATION_CLOSED, TournamentStatus.IN_PROGRESS);
    }

    public void complete() throws InvalidOperationException { move(TournamentStatus.IN_PROGRESS, TournamentStatus.COMPLETED); }

    private void move(TournamentStatus from, TournamentStatus to) throws InvalidOperationException {
        if (status != from) {
            throw new InvalidOperationException(name + " is " + status + "; it must be " + from + " to become " + to);
        }
        status = to;
    }

    // ---- registration ------------------------------------------------------------

    /**
     * Adds the registration only if every rule passes (status, deadline, sport, eligibility,
     * duplicate, capacity). Nothing is modified before the last check, so failures never
     * leave a half-registered entry.
     *
     * @param today passed in (not read from the clock) so deadline rules are testable
     */
    public void addRegistration(Registration reg, LocalDate today)
            throws DuplicateRegistrationException, TournamentFullException,
                   IneligiblePlayerException, InvalidOperationException {
        Validator.requireNonNull(reg, "Registration");
        if (status != TournamentStatus.REGISTRATION_OPEN) {
            throw new InvalidOperationException("registration for " + name + " is not open (status " + status + ")");
        }
        if (today.isAfter(registrationDeadline)) {
            throw new InvalidOperationException("registration deadline " + registrationDeadline + " has passed");
        }
        if (reg.isTeamEntry()) {
            if (reg.getTeam().getSport().getType() != sport.getType()) {
                throw new InvalidOperationException(reg.getTeam().getName() + " plays "
                        + reg.getTeam().getSport() + ", not " + sport);
            }
            if (!reg.getTeam().isMatchReady()) {
                throw new InvalidOperationException(reg.getTeam().getName() + " needs at least "
                        + sport.getMinTeamSize() + " players to enter");
            }
        } else {
            sport.checkEligibility(reg.getPlayer());
        }
        Set<String> alreadyIn = activePlayerIds();
        for (String playerId : reg.coveredPlayerIds()) {
            if (alreadyIn.contains(playerId)) {
                throw new DuplicateRegistrationException(nameOfPlayer(reg, playerId), name);
            }
        }
        if (isFull()) {
            throw new TournamentFullException(name, maxEntries);
        }
        registrations.add(reg);
    }

    /** HashSet gives O(1) membership checks while scanning the new entry's players. */
    private Set<String> activePlayerIds() {
        Set<String> ids = new HashSet<>();
        for (Registration r : registrations) {
            if (r.isActive()) ids.addAll(r.coveredPlayerIds());
        }
        return ids;
    }

    private String nameOfPlayer(Registration reg, String playerId) {
        if (reg.getPlayer() != null) return reg.getPlayer().getName();
        for (Player p : reg.getTeam().getRoster()) {
            if (p.getId().equals(playerId)) return p.getName();
        }
        return playerId;
    }

    public int activeEntryCount() {
        int n = 0;
        for (Registration r : registrations) if (r.isActive()) n++;
        return n;
    }

    public boolean isFull() { return activeEntryCount() >= maxEntries; }

    /** Removes a registration that never completed (used to roll back after a failed payment). */
    public void removeRegistration(Registration reg) { registrations.remove(reg); }

    /**
     * Persistence support: re-attaches an already-built Registration and restores the
     * saved lifecycle status when loading data. The registration's own rules (deadline,
     * duplicates, capacity) do not apply here because the entry was accepted when it was
     * originally made; re-running them would make old data unloadable.
     */
    public void restoreRegistration(Registration reg, TournamentStatus savedStatus)
            throws InvalidOperationException {
        Validator.requireNonNull(reg, "Registration");
        Validator.requireNonNull(savedStatus, "Tournament status");
        for (Registration existing : registrations) {
            if (existing.getRegistrationId().equalsIgnoreCase(reg.getRegistrationId())) {
                throw new InvalidOperationException("registration " + reg.getRegistrationId() + " already exists in " + name);
            }
        }
        registrations.add(reg);
        this.status = savedStatus;
    }

    /** Fee polymorphism: works for any Person subclass through the superclass reference. */
    public double feeFor(Person person) { return person.calculateFee() * feeMultiplier; }

    /** Overload: a team pays the sum of its players' fees. */
    public double feeFor(Team team) {
        double total = 0;
        for (Player p : team.getRoster()) total += feeFor((Person) p);
        return total;
    }

    public List<Team> getConfirmedTeams() {
        List<Team> teams = new ArrayList<>();
        for (Registration r : registrations) {
            if (r.isTeamEntry() && r.getStatus() == RegistrationStatus.CONFIRMED) teams.add(r.getTeam());
        }
        return teams;
    }

    public List<Registration> getRegistrations() { return Collections.unmodifiableList(registrations); }
    public String getTournamentId() { return tournamentId; }
    public String getName() { return name; }
    public Sport getSport() { return sport; }
    public LocalDate getRegistrationDeadline() { return registrationDeadline; }
    public int getMaxEntries() { return maxEntries; }
    public double getFeeMultiplier() { return feeMultiplier; }
    public TournamentStatus getStatus() { return status; }

    @Override
    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("TOURNAMENT %s: %s (%s) | Status %s | Deadline %s | Entries %d/%d%n",
                tournamentId, name, sport, status, registrationDeadline, activeEntryCount(), maxEntries));
        if (registrations.isEmpty()) sb.append("   (no registrations)\n");
        for (Registration r : registrations) {
            sb.append("   ").append(r).append('\n');
        }
        return sb.toString();
    }

    @Override
    public String toString() { return tournamentId + " " + name; }
}
