package com.thapar.sportssphere.service;

import com.thapar.sportssphere.exception.InvalidOperationException;
import com.thapar.sportssphere.exception.InvalidScoreException;
import com.thapar.sportssphere.exception.SportsException;
import com.thapar.sportssphere.model.Match;
import com.thapar.sportssphere.model.SportsData;
import com.thapar.sportssphere.model.SportsFacility;
import com.thapar.sportssphere.model.Team;
import com.thapar.sportssphere.model.Tournament;
import com.thapar.sportssphere.model.enums.MatchStatus;
import com.thapar.sportssphere.model.enums.TournamentStatus;
import com.thapar.sportssphere.util.IdGenerator;

import java.time.LocalDateTime;

/** Scheduling (teams + venue conflicts), scoring and corrections. */
public class MatchService {
    private final SportsData data;
    private final BookingService bookings;
    private final AnalyticsService analytics;

    public MatchService(SportsData data, BookingService bookings, AnalyticsService analytics) {
        this.data = data;
        this.bookings = bookings;
        this.analytics = analytics;
    }

    /**
     * Order is chosen so a failure never leaves partial state: build and validate the match,
     * check team clashes, and only then reserve the venue; the match is stored last.
     */
    public Match schedule(String tournamentId, String teamAId, String teamBId, String facilityId,
                          LocalDateTime start) throws SportsException {
        Tournament tournament = data.findTournament(tournamentId);
        if (tournament.getStatus() != TournamentStatus.IN_PROGRESS) {
            throw new InvalidOperationException(tournament.getName() + " is " + tournament.getStatus()
                    + "; matches can only be scheduled while it is IN_PROGRESS");
        }
        Team a = data.findTeam(teamAId);
        Team b = data.findTeam(teamBId);
        requireConfirmed(tournament, a);
        requireConfirmed(tournament, b);
        SportsFacility venue = data.findFacility(facilityId);

        Match match = new Match(IdGenerator.next("MAT"), tournament.getTournamentId(), a, b, venue, start);

        for (Match existing : data.getMatches()) {
            boolean sameTeam = existing.involves(a) || existing.involves(b);
            if (existing.getStatus() != MatchStatus.CANCELLED && sameTeam
                    && start.isBefore(existing.getEnd()) && existing.getStart().isBefore(match.getEnd())) {
                throw new InvalidOperationException("a team is already playing " + existing.getMatchId()
                        + " at an overlapping time");
            }
        }
        bookings.book(venue.getFacilityId(), bookingIdFor(match), start, match.getEnd(),
                "Match " + match.getMatchId(), "Match scheduler");   // throws FacilityUnavailableException on clash
        data.addMatch(match);
        return match;
    }

    private static void requireConfirmed(Tournament t, Team team) throws InvalidOperationException {
        for (Team confirmed : t.getConfirmedTeams()) {
            if (confirmed.getTeamId().equals(team.getTeamId())) return;
        }
        throw new InvalidOperationException(team.getName() + " is not a confirmed entry in " + t.getName());
    }

    private static String bookingIdFor(Match m) { return "BK-" + m.getMatchId(); }

    /** First result. recordScore throws before anything changes, so stats are updated only on success. */
    public void enterScore(String matchId, int scoreA, int scoreB) throws SportsException {
        Match match = data.findMatch(matchId);
        Tournament t = data.findTournament(match.getTournamentId());
        if (t.getStatus() != TournamentStatus.IN_PROGRESS) {
            throw new InvalidOperationException("scores can only be entered while " + t.getName() + " is IN_PROGRESS");
        }
        match.recordScore(scoreA, scoreB);
        analytics.applyResult(match);
    }

    /** The explicit correction process required before a completed result may change. */
    public void correctScore(String matchId, int scoreA, int scoreB, String reason)
            throws InvalidScoreException, InvalidOperationException {
        Match match = data.findMatch(matchId);
        match.correctScore(scoreA, scoreB, reason);
        analytics.rebuildPlayerStats();
    }

    public void cancelMatch(String matchId) throws InvalidOperationException {
        Match match = data.findMatch(matchId);
        match.cancel();                                              // rejects completed matches
        bookings.cancel(match.getVenue().getFacilityId(), bookingIdFor(match));   // free the venue slot
    }
}
