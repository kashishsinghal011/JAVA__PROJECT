package com.thapar.sportssphere.model;

import com.thapar.sportssphere.model.contract.Registrable;
import com.thapar.sportssphere.model.enums.MatchOutcome;
import com.thapar.sportssphere.model.enums.SportType;
import com.thapar.sportssphere.util.Validator;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/** A student athlete: preferences, sports-office registration status and performance stats. */
public class Player extends Person implements Registrable {
    private static final long serialVersionUID = 1L;

    /** Base fee for a player; final so no code can change the fee policy at runtime. */
    public static final double BASE_FEE = 200.0;
    public static final double EARLY_BIRD_DISCOUNT = 0.10;

    private final String rollNo;
    // EnumSet: compact, ordered by enum, O(1) contains.
    private final Set<SportType> preferences = EnumSet.noneOf(SportType.class);
    private boolean registered;

    private int matchesPlayed;
    private int matchesWon;
    private int matchesLost;
    private int matchesDrawn;
    private int pointsScored;

    public Player(String id, String name, int age, String contact, String rollNo,
                  Collection<SportType> preferences, boolean registered) {
        super(id, name, age, contact);                       // super(...) reuses Person validation
        this.rollNo = Validator.requireNonBlank(rollNo, "Roll number");
        if (preferences != null) {
            this.preferences.addAll(preferences);
        }
        this.registered = registered;
    }

    /** Convenience overload: one favourite sport, not yet registered with the sports office. */
    public Player(String id, String name, int age, String rollNo, SportType favourite) {
        this(id, name, age, "N/A", rollNo, EnumSet.of(Validator.requireNonNull(favourite, "Sport")), false);
    }

    public String getRollNo() { return rollNo; }

    /** Returns a read-only view so callers cannot bypass addPreference(). */
    public Set<SportType> getPreferences() { return Collections.unmodifiableSet(preferences); }

    public void addPreference(SportType sport) {
        preferences.add(Validator.requireNonNull(sport, "Sport"));
    }

    public boolean prefers(SportType sport) { return preferences.contains(sport); }

    @Override public boolean isRegistered() { return registered; }
    @Override public void markRegistered() { this.registered = true; }
    @Override public void revokeRegistration() { this.registered = false; }

    // ---- performance statistics -------------------------------------------------

    public void recordResult(MatchOutcome outcome, int pointsScored) {
        Validator.requireNonNull(outcome, "Outcome");
        this.pointsScored += Validator.requireNonNegative(pointsScored, "Points");
        matchesPlayed++;
        switch (outcome) {
            case WIN:  matchesWon++;   break;
            case LOSS: matchesLost++;  break;
            default:   matchesDrawn++; break;
        }
    }

    /** Overload: result known, individual points not tracked for this sport. */
    public void recordResult(MatchOutcome outcome) { recordResult(outcome, 0); }

    /** Used before replaying all matches, e.g. after a score correction. */
    public void resetStats() {
        matchesPlayed = matchesWon = matchesLost = matchesDrawn = pointsScored = 0;
    }

    /**
     * Persistence support: restores exact statistics counters when loading saved data.
     * The saved values were produced by the domain itself, so negative or incoherent
     * counts are treated as corrupted data and rejected.
     */
    public void restoreStats(int played, int won, int lost, int drawn, int points) {
        if (played < 0 || won < 0 || lost < 0 || drawn < 0 || points < 0
                || won + lost + drawn != played) {
            throw new IllegalArgumentException("corrupted statistics for player " + getId());
        }
        matchesPlayed = played;
        matchesWon = won;
        matchesLost = lost;
        matchesDrawn = drawn;
        pointsScored = points;
    }

    public int getMatchesPlayed() { return matchesPlayed; }
    public int getMatchesWon() { return matchesWon; }
    public int getMatchesLost() { return matchesLost; }
    public int getMatchesDrawn() { return matchesDrawn; }
    public int getPointsScored() { return pointsScored; }

    /** Guards division by zero: a player with no matches has 0%, not NaN. */
    public double winPercentage() {
        return matchesPlayed == 0 ? 0.0 : (matchesWon * 100.0) / matchesPlayed;
    }

    // ---- polymorphic behaviour --------------------------------------------------

    @Override
    public double calculateFee() { return BASE_FEE; }

    /** Overload (different parameter list, not an override): early-bird discount. */
    public double calculateFee(boolean earlyBird) {
        return earlyBird ? BASE_FEE * (1 - EARLY_BIRD_DISCOUNT) : BASE_FEE;
    }

    @Override
    public void displayDetails() {
        System.out.println("[PLAYER] " + baseDetails());
        System.out.printf("   Roll No: %s | Sports: %s | Sports-office registered: %s%n",
                rollNo, preferences, registered ? "Yes" : "No");
        System.out.printf("   Played %d | W %d | L %d | D %d | Points %d | Win %.1f%%%n",
                matchesPlayed, matchesWon, matchesLost, matchesDrawn, pointsScored, winPercentage());
    }
}
