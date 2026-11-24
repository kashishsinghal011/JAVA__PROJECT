package com.thapar.sportssphere.service;

import com.thapar.sportssphere.exception.InvalidOperationException;
import com.thapar.sportssphere.model.FacilityBooking;
import com.thapar.sportssphere.model.Match;
import com.thapar.sportssphere.model.Person;
import com.thapar.sportssphere.model.Player;
import com.thapar.sportssphere.model.SportsData;
import com.thapar.sportssphere.model.SportsFacility;
import com.thapar.sportssphere.model.Team;
import com.thapar.sportssphere.model.Tournament;

import java.util.List;

/** Builds readable text reports. Returns strings so the menu (or a test) decides where to print. */
public class ReportService {
    private final SportsData data;
    private final AnalyticsService analytics;

    public ReportService(SportsData data, AnalyticsService analytics) {
        this.data = data;
        this.analytics = analytics;
    }

    /** POLYMORPHISM in practice: one loop over Person references, each calculateFee() resolved at runtime. */
    public String peopleDirectory() {
        StringBuilder sb = new StringBuilder("PEOPLE DIRECTORY\n");
        for (Person p : data.getAllPeople()) {
            sb.append(String.format("   %-4s %-20s %-18s fee Rs %.2f%n",
                    p.getId(), p.getName(), p.getClass().getSimpleName(), p.calculateFee()));
        }
        return sb.toString();
    }

    public String playerProfiles() {
        StringBuilder sb = new StringBuilder("PLAYER PROFILES\n");
        if (data.getPlayers().isEmpty()) sb.append("   (no players)\n");
        for (Player p : data.getPlayers()) {
            sb.append(playerProfile(p));
        }
        return sb.toString();
    }

    public String playerProfile(Player p) {
        return String.format("%s | %s | Age %d | Roll %s | Sports %s | Office-registered %s%n"
                        + "   Played %d | W %d | L %d | D %d | Win %.1f%%%n",
                p.getId(), p.getName(), p.getAge(), p.getRollNo(), p.getPreferences(),
                p.isRegistered() ? "Yes" : "No",
                p.getMatchesPlayed(), p.getMatchesWon(), p.getMatchesLost(), p.getMatchesDrawn(), p.winPercentage());
    }

    public String teamRosters() {
        StringBuilder sb = new StringBuilder("TEAM ROSTERS\n");
        if (data.getTeams().isEmpty()) sb.append("   (no teams)\n");
        for (Team t : data.getTeams()) sb.append(t.generateReport());
        return sb.toString();
    }

    public String tournamentRegistrations() {
        StringBuilder sb = new StringBuilder("TOURNAMENT REGISTRATIONS\n");
        if (data.getTournaments().isEmpty()) sb.append("   (no tournaments)\n");
        for (Tournament t : data.getTournaments()) sb.append(t.generateReport());
        return sb.toString();
    }

    public String facilitySchedules() {
        StringBuilder sb = new StringBuilder("FACILITY BOOKING SCHEDULES\n");
        for (SportsFacility f : data.getFacilities()) {
            sb.append(String.format("%s %s | open %s-%s%n", f.getFacilityId(), f, f.getOpensAt(), f.getClosesAt()));
            if (f.getBookings().isEmpty()) sb.append("   (no bookings)\n");
            for (FacilityBooking b : f.getBookings()) sb.append("   ").append(b).append('\n');
        }
        if (data.getFacilities().isEmpty()) sb.append("   (no facilities)\n");
        return sb.toString();
    }

    public String equipmentAvailability() { return data.getInventory().generateReport(); }

    public String fixturesAndResults() {
        StringBuilder sb = new StringBuilder("MATCH FIXTURES AND RESULTS\n");
        if (data.getMatches().isEmpty()) sb.append("   (no matches)\n");
        for (Match m : data.getMatches()) {
            sb.append("   ").append(m).append('\n');
            for (String note : m.getAuditLog()) sb.append("       correction: ").append(note).append('\n');
        }
        return sb.toString();
    }

    public String standings(String tournamentId) throws InvalidOperationException {
        Tournament t = data.findTournament(tournamentId);
        List<TeamStanding> rows = analytics.standings(tournamentId);
        StringBuilder sb = new StringBuilder("STANDINGS - " + t.getName() + "\n");
        sb.append(String.format("%-4s %-20s %3s %3s %3s %3s %4s %4s %4s %4s %7s%n",
                "Pos", "Team", "P", "W", "D", "L", "GF", "GA", "GD", "Pts", "Win%"));
        int pos = 1;
        for (TeamStanding s : rows) {
            sb.append(String.format("%-4d %-20s %3d %3d %3d %3d %4d %4d %4d %4d %6.1f%%%n", pos++, s.getTeamName(),
                    s.getPlayed(), s.getWon(), s.getDrawn(), s.getLost(), s.getScored(), s.getConceded(),
                    s.goalDifference(), s.points(), s.winPercentage()));
        }
        if (rows.isEmpty()) sb.append("   (no confirmed teams)\n");
        sb.append("Ranking: points, wins, goal difference, goals scored, team name.\n");
        return sb.toString();
    }

    public String topPlayers(int limit) {
        StringBuilder sb = new StringBuilder("TOP PLAYERS (by win percentage)\n");
        List<Player> top = analytics.topPlayers(limit);
        if (top.isEmpty()) sb.append("   (no player has played a match yet)\n");
        int rank = 1;
        for (Player p : top) {
            sb.append(String.format("%3d. %-20s played %d, won %d, win %.1f%%%n", rank++, p.getName(),
                    p.getMatchesPlayed(), p.getMatchesWon(), p.winPercentage()));
        }
        return sb.toString();
    }

    public String tournamentSummary(String tournamentId) throws InvalidOperationException {
        Tournament t = data.findTournament(tournamentId);
        int scheduled = 0;
        int completed = 0;
        for (Match m : data.getMatches()) {
            if (!m.getTournamentId().equals(t.getTournamentId())) continue;
            scheduled++;
            if (m.isCompleted()) completed++;
        }
        List<TeamStanding> table = analytics.standings(tournamentId);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SUMMARY %s: %s (%s)%nStatus %s | Entries %d/%d | Matches %d scheduled, %d completed%n",
                t.getTournamentId(), t.getName(), t.getSport(), t.getStatus(), t.activeEntryCount(),
                t.getMaxEntries(), scheduled, completed));
        sb.append(table.isEmpty() || table.get(0).getPlayed() == 0
                ? "Leader: none yet\n"
                : "Leader: " + table.get(0).getTeamName() + " with " + table.get(0).points() + " points\n");
        return sb.toString();
    }

    public String failedOperations() { return data.getFailureLog().generateReport(); }
}
