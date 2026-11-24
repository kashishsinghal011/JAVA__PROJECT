package com.thapar.sportssphere.service;

import com.thapar.sportssphere.exception.InvalidOperationException;
import com.thapar.sportssphere.model.Match;
import com.thapar.sportssphere.model.Player;
import com.thapar.sportssphere.model.SportsData;
import com.thapar.sportssphere.model.Team;
import com.thapar.sportssphere.model.enums.MatchOutcome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Standings and player statistics. All sorting is O(n log n); each pass over matches is O(m). */
public class AnalyticsService {
    private final SportsData data;

    public AnalyticsService(SportsData data) { this.data = data; }

    /** Credits every player on each roster with the match result. */
    public void applyResult(Match match) throws InvalidOperationException {
        MatchOutcome outcomeA = match.getOutcomeForTeamA();
        for (Player p : match.getTeamA().getRoster()) p.recordResult(outcomeA);
        for (Player p : match.getTeamB().getRoster()) p.recordResult(outcomeA.opposite());
    }

    /**
     * Recomputes every player's statistics from completed matches. Used after a score
     * correction so stats can never drift from the stored results.
     * (Known limitation: uses current rosters, so later roster changes are reflected.)
     */
    public void rebuildPlayerStats() throws InvalidOperationException {
        for (Player p : data.getPlayers()) p.resetStats();
        for (Match m : data.getMatches()) {
            if (m.isCompleted()) applyResult(m);
        }
    }

    /** Table for one tournament. Confirmed teams with no matches still appear, with zeros. */
    public List<TeamStanding> standings(String tournamentId) throws InvalidOperationException {
        Map<String, TeamStanding> table = new LinkedHashMap<>();
        for (Team t : data.findTournament(tournamentId).getConfirmedTeams()) {
            table.put(t.getTeamId(), new TeamStanding(t.getTeamId(), t.getName()));
        }
        String tid = data.findTournament(tournamentId).getTournamentId();
        for (Match m : data.getMatches()) {
            if (!m.isCompleted() || !m.getTournamentId().equals(tid)) continue;
            TeamStanding a = table.get(m.getTeamA().getTeamId());
            TeamStanding b = table.get(m.getTeamB().getTeamId());
            if (a != null) a.record(m.getScoreA(), m.getScoreB());
            if (b != null) b.record(m.getScoreB(), m.getScoreA());
        }
        List<TeamStanding> rows = new ArrayList<>(table.values());
        Collections.sort(rows);
        return rows;
    }

    /** Players with at least one match, best win percentage first (ties: wins, matches, name). */
    public List<Player> topPlayers(int limit) {
        List<Player> ranked = new ArrayList<>();
        for (Player p : data.getPlayers()) {
            if (p.getMatchesPlayed() > 0) ranked.add(p);
        }
        ranked.sort(Comparator.comparingDouble(Player::winPercentage).reversed()
                .thenComparing(Comparator.comparingInt(Player::getMatchesWon).reversed())
                .thenComparing(Comparator.comparingInt(Player::getMatchesPlayed).reversed())
                .thenComparing(Player::getName, String.CASE_INSENSITIVE_ORDER));
        return ranked.size() > limit ? new ArrayList<>(ranked.subList(0, Math.max(limit, 0))) : ranked;
    }
}
