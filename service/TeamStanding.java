package com.thapar.sportssphere.service;

/**
 * One row of a tournament table. Ranking order (compareTo):
 * points, then wins, then goal difference, then goals scored, then team name (A-Z).
 */
public class TeamStanding implements Comparable<TeamStanding> {
    public static final int POINTS_WIN = 3;
    public static final int POINTS_DRAW = 1;

    private final String teamId;
    private final String teamName;
    private int played;
    private int won;
    private int drawn;
    private int lost;
    private int scored;
    private int conceded;

    public TeamStanding(String teamId, String teamName) {
        this.teamId = teamId;
        this.teamName = teamName;
    }

    public void record(int goalsFor, int goalsAgainst) {
        played++;
        scored += goalsFor;
        conceded += goalsAgainst;
        if (goalsFor > goalsAgainst) won++;
        else if (goalsFor < goalsAgainst) lost++;
        else drawn++;
    }

    public int points() { return won * POINTS_WIN + drawn * POINTS_DRAW; }
    public int goalDifference() { return scored - conceded; }

    /** Guarded: a team with no matches has 0%, never a division by zero. */
    public double winPercentage() { return played == 0 ? 0.0 : (won * 100.0) / played; }

    @Override
    public int compareTo(TeamStanding o) {
        int c = Integer.compare(o.points(), points());
        if (c != 0) return c;
        c = Integer.compare(o.won, won);
        if (c != 0) return c;
        c = Integer.compare(o.goalDifference(), goalDifference());
        if (c != 0) return c;
        c = Integer.compare(o.scored, scored);
        if (c != 0) return c;
        return teamName.compareToIgnoreCase(o.teamName);
    }

    public String getTeamId() { return teamId; }
    public String getTeamName() { return teamName; }
    public int getPlayed() { return played; }
    public int getWon() { return won; }
    public int getDrawn() { return drawn; }
    public int getLost() { return lost; }
    public int getScored() { return scored; }
    public int getConceded() { return conceded; }
}
