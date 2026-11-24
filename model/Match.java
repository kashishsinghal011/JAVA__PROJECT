package com.thapar.sportssphere.model;

import com.thapar.sportssphere.exception.InvalidOperationException;
import com.thapar.sportssphere.exception.InvalidScoreException;
import com.thapar.sportssphere.model.enums.MatchOutcome;
import com.thapar.sportssphere.model.enums.MatchStatus;
import com.thapar.sportssphere.util.Validator;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * One fixture between two teams at a venue. ASSOCIATION: refers to teams and a facility that
 * live on independently. Scoring rules (no negative score, no re-scoring, draw rules) live here.
 */
public class Match implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MAX_SCORE = 500;
    public static final int DEFAULT_DURATION_MINUTES = 90;

    private final String matchId;
    private final String tournamentId;
    private final Team teamA;
    private final Team teamB;
    private final SportsFacility venue;
    private final LocalDateTime start;
    private int scoreA;
    private int scoreB;
    private MatchStatus status = MatchStatus.SCHEDULED;
    private final List<String> auditLog = new ArrayList<>();

    public Match(String matchId, String tournamentId, Team teamA, Team teamB,
                 SportsFacility venue, LocalDateTime start) throws InvalidOperationException {
        this.matchId = Validator.requireNonBlank(matchId, "Match id");
        this.tournamentId = Validator.requireNonBlank(tournamentId, "Tournament id");
        this.teamA = Validator.requireNonNull(teamA, "Team A");
        this.teamB = Validator.requireNonNull(teamB, "Team B");
        this.venue = Validator.requireNonNull(venue, "Venue");
        this.start = Validator.requireNonNull(start, "Start time");
        if (teamA.getTeamId().equals(teamB.getTeamId())) {
            throw new InvalidOperationException("a team cannot play against itself");
        }
        if (teamA.getSport().getType() != teamB.getSport().getType()) {
            throw new InvalidOperationException("teams play different sports");
        }
        if (venue.getType() != teamA.getSport().getType()) {
            throw new InvalidOperationException(venue.getName() + " is a " + venue.getType() + " facility, not for "
                    + teamA.getSport());
        }
    }

    /** First and only normal scoring. A second call is rejected; use correctScore instead. */
    public void recordScore(int a, int b) throws InvalidScoreException, InvalidOperationException {
        if (status == MatchStatus.COMPLETED) {
            throw new InvalidOperationException("match " + matchId + " already has a result; use the correction process");
        }
        if (status == MatchStatus.CANCELLED) {
            throw new InvalidOperationException("match " + matchId + " was cancelled");
        }
        validateScore(a, b);
        scoreA = a;
        scoreB = b;
        status = MatchStatus.COMPLETED;
    }

    /** Explicit, audited correction of a completed result. */
    public void correctScore(int a, int b, String reason) throws InvalidScoreException, InvalidOperationException {
        if (status != MatchStatus.COMPLETED) {
            throw new InvalidOperationException("match " + matchId + " has no result to correct");
        }
        if (reason == null || reason.isBlank()) {
            throw new InvalidOperationException("a reason is required to correct a result");
        }
        validateScore(a, b);
        auditLog.add(String.format("%d-%d changed to %d-%d (%s)", scoreA, scoreB, a, b, reason.trim()));
        scoreA = a;
        scoreB = b;
    }

    private void validateScore(int a, int b) throws InvalidScoreException {
        if (a < 0 || b < 0) {
            throw new InvalidScoreException("scores cannot be negative (got " + a + " - " + b + ")");
        }
        if (a > MAX_SCORE || b > MAX_SCORE) {
            throw new InvalidScoreException("scores above " + MAX_SCORE + " are not realistic");
        }
        if (a == b && !teamA.getSport().isDrawsAllowed()) {
            throw new InvalidScoreException(teamA.getSport() + " does not allow draws (got " + a + " - " + b + ")");
        }
    }

    public void cancel() throws InvalidOperationException {
        if (status == MatchStatus.COMPLETED) {
            throw new InvalidOperationException("a completed match cannot be cancelled");
        }
        status = MatchStatus.CANCELLED;
    }

    public boolean isCompleted() { return status == MatchStatus.COMPLETED; }

    public boolean involves(Team team) {
        return teamA.getTeamId().equals(team.getTeamId()) || teamB.getTeamId().equals(team.getTeamId());
    }

    public LocalDateTime getEnd() { return start.plusMinutes(DEFAULT_DURATION_MINUTES); }

    /** Result seen from team A; call only on a completed match. */
    public MatchOutcome getOutcomeForTeamA() throws InvalidOperationException {
        if (!isCompleted()) {
            throw new InvalidOperationException("match " + matchId + " is not completed");
        }
        if (scoreA > scoreB) return MatchOutcome.WIN;
        if (scoreA < scoreB) return MatchOutcome.LOSS;
        return MatchOutcome.DRAW;
    }

    public Optional<Team> getWinner() {
        if (!isCompleted() || scoreA == scoreB) return Optional.empty();
        return Optional.of(scoreA > scoreB ? teamA : teamB);
    }

    public String getMatchId() { return matchId; }

    /**
     * Persistence support: appends a saved audit-log note while loading data and restores
     * the exact status/scores of a completed match. Validation mirrors the live rules so a
     * corrupted row cannot create an impossible match state.
     */
    public void restoreResult(int a, int b, MatchStatus savedStatus, List<String> savedAuditLog)
            throws InvalidScoreException, InvalidOperationException {
        Validator.requireNonNull(savedStatus, "Saved status");
        if (savedStatus != MatchStatus.COMPLETED && savedStatus != MatchStatus.CANCELLED
                && savedStatus != MatchStatus.SCHEDULED) {
            throw new InvalidOperationException("unsupported persisted status " + savedStatus);
        }
        if (savedStatus == MatchStatus.COMPLETED) {
            validateScore(a, b);
            scoreA = a;
            scoreB = b;
        } else if (savedStatus == MatchStatus.CANCELLED) {
            cancel();
        }
        if (savedAuditLog != null) {
            for (String note : savedAuditLog) {
                if (note != null && !note.isBlank()) {
                    auditLog.add(note.trim());
                }
            }
        }
    }
    public String getTournamentId() { return tournamentId; }
    public Team getTeamA() { return teamA; }
    public Team getTeamB() { return teamB; }
    public SportsFacility getVenue() { return venue; }
    public LocalDateTime getStart() { return start; }
    public int getScoreA() { return scoreA; }
    public int getScoreB() { return scoreB; }
    public MatchStatus getStatus() { return status; }
    public List<String> getAuditLog() { return Collections.unmodifiableList(auditLog); }

    @Override
    public String toString() {
        String when = start.format(DateTimeFormatter.ofPattern("dd-MMM HH:mm"));
        String result = isCompleted() ? scoreA + " - " + scoreB : "vs";
        return String.format("%s | %s | %s %s %s @ %s [%s]", matchId, when, teamA.getName(), result,
                teamB.getName(), venue.getName(), status);
    }
}
