package com.thapar.sportssphere.service;

import com.thapar.sportssphere.exception.DuplicateRegistrationException;
import com.thapar.sportssphere.exception.IneligiblePlayerException;
import com.thapar.sportssphere.exception.InvalidOperationException;
import com.thapar.sportssphere.exception.PaymentFailedException;
import com.thapar.sportssphere.exception.PlayerNotFoundException;
import com.thapar.sportssphere.exception.SportsException;
import com.thapar.sportssphere.exception.TournamentFullException;
import com.thapar.sportssphere.model.Player;
import com.thapar.sportssphere.model.Registration;
import com.thapar.sportssphere.model.Sport;
import com.thapar.sportssphere.model.SportsData;
import com.thapar.sportssphere.model.Team;
import com.thapar.sportssphere.model.Tournament;
import com.thapar.sportssphere.model.enums.RegistrationStatus;
import com.thapar.sportssphere.model.enums.SportType;
import com.thapar.sportssphere.util.IdGenerator;

import java.time.Clock;
import java.time.LocalDate;

/** Tournament set-up and registration, including the all-or-nothing register-and-pay flow. */
public class RegistrationService {
    private final SportsData data;
    private final Clock clock;
    private final PaymentService payments;

    public RegistrationService(SportsData data, Clock clock, PaymentService payments) {
        this.data = data;
        this.clock = clock;
        this.payments = payments;
    }

    // ---- tournament management ---------------------------------------------------

    public Tournament createTournament(String name, SportType type, LocalDate deadline,
                                       int maxEntries, double feeMultiplier) throws InvalidOperationException {
        if (deadline.isBefore(LocalDate.now(clock))) {
            throw new InvalidOperationException("registration deadline " + deadline + " is already in the past");
        }
        Tournament t = new Tournament(IdGenerator.next("TRN"), name, Sport.standard(type), deadline, maxEntries, feeMultiplier);
        data.addTournament(t);
        return t;
    }

    public void openRegistration(String tournamentId) throws InvalidOperationException { data.findTournament(tournamentId).openRegistration(); }
    public void closeRegistration(String tournamentId) throws InvalidOperationException { data.findTournament(tournamentId).closeRegistration(); }
    public void startTournament(String tournamentId) throws InvalidOperationException { data.findTournament(tournamentId).start(); }
    public void completeTournament(String tournamentId) throws InvalidOperationException { data.findTournament(tournamentId).complete(); }

    // ---- reserve a slot (PENDING) --------------------------------------------------

    public Registration registerPlayer(String tournamentId, String playerId)
            throws DuplicateRegistrationException, TournamentFullException, IneligiblePlayerException,
                   InvalidOperationException, PlayerNotFoundException {
        LocalDate today = LocalDate.now(clock);
        Tournament t = data.findTournament(tournamentId);
        Player p = data.findPlayer(playerId);
        Registration r = Registration.forPlayer(IdGenerator.next("REG"), p, t.getTournamentId(), t.feeFor(p), today);
        t.addRegistration(r, today);            // all rules are checked inside; nothing is stored on failure
        return r;
    }

    public Registration registerTeam(String tournamentId, String teamId)
            throws DuplicateRegistrationException, TournamentFullException, IneligiblePlayerException,
                   InvalidOperationException {
        LocalDate today = LocalDate.now(clock);
        Tournament t = data.findTournament(tournamentId);
        Team team = data.findTeam(teamId);
        Registration r = Registration.forTeam(IdGenerator.next("REG"), team, t.getTournamentId(), t.feeFor(team), today);
        t.addRegistration(r, today);
        return r;
    }

    // ---- register + pay, all or nothing ------------------------------------------------

    /**
     * If the simulated payment fails, the reserved slot is released again, so a failed
     * payment leaves tournament data exactly as it was before the attempt.
     */
    public Registration registerPlayerAndPay(String tournamentId, String playerId, boolean gatewayApproves)
            throws SportsException {
        Registration r = registerPlayer(tournamentId, playerId);
        return payOrRollback(data.findTournament(tournamentId), r, gatewayApproves);
    }

    public Registration registerTeamAndPay(String tournamentId, String teamId, boolean gatewayApproves)
            throws SportsException {
        Registration r = registerTeam(tournamentId, teamId);
        return payOrRollback(data.findTournament(tournamentId), r, gatewayApproves);
    }

    private Registration payOrRollback(Tournament t, Registration r, boolean gatewayApproves)
            throws PaymentFailedException, InvalidOperationException {
        try {
            payments.settle(r, gatewayApproves);
            return r;
        } catch (PaymentFailedException | InvalidOperationException e) {
            t.removeRegistration(r);            // roll back: no half-registered entry survives
            throw e;
        }
    }

    public void cancelRegistration(String registrationId) throws InvalidOperationException {
        Registration r = data.findRegistration(registrationId);
        if (r.getStatus() == RegistrationStatus.CANCELLED) {
            throw new InvalidOperationException("registration " + r.getRegistrationId() + " is already cancelled");
        }
        r.cancel();                             // inactive registrations no longer count toward capacity
    }
}
