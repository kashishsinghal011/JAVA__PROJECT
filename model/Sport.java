package com.thapar.sportssphere.model;

import com.thapar.sportssphere.exception.IneligiblePlayerException;
import com.thapar.sportssphere.model.enums.SportType;
import com.thapar.sportssphere.util.Validator;

import java.io.Serializable;

/** Rules of one sport: team-size limits, age band and whether a match may end in a draw. */
public class Sport implements Serializable {
    private static final long serialVersionUID = 1L;

    private final SportType type;
    private final int minTeamSize;
    private final int maxTeamSize;
    private final int minAge;
    private final int maxAge;
    private final boolean drawsAllowed;
    private final String rules;

    public Sport(SportType type, int minTeamSize, int maxTeamSize, int minAge, int maxAge,
                 boolean drawsAllowed, String rules) {
        this.type = Validator.requireNonNull(type, "Sport type");
        this.minTeamSize = Validator.requirePositive(minTeamSize, "Minimum team size");
        if (maxTeamSize < minTeamSize) {
            throw new IllegalArgumentException("Maximum team size cannot be below minimum team size.");
        }
        if (maxAge < minAge) {
            throw new IllegalArgumentException("Maximum age cannot be below minimum age.");
        }
        this.maxTeamSize = maxTeamSize;
        this.minAge = Validator.requireRange(minAge, Person.MIN_AGE, Person.MAX_AGE, "Minimum age");
        this.maxAge = Validator.requireRange(maxAge, Person.MIN_AGE, Person.MAX_AGE, "Maximum age");
        this.drawsAllowed = drawsAllowed;
        this.rules = Validator.requireNonBlank(rules, "Rules");
    }

    /** Factory with sensible campus defaults (age band 17-28) so callers need not repeat numbers. */
    public static Sport standard(SportType type) {
        switch (type) {
            case FOOTBALL:     return new Sport(type, 7, 15, 17, 28, true,  "90 minutes, 11 a side; draws allowed in league stage.");
            case BASKETBALL:   return new Sport(type, 5, 12, 17, 28, false, "4 quarters; overtime decides ties, so no draws.");
            case BADMINTON:    return new Sport(type, 1, 4,  17, 28, false, "Best of three games to 21; no draws.");
            case CRICKET:      return new Sport(type, 11, 15, 17, 28, true, "Limited-overs match; ties/no-result count as draw.");
            case VOLLEYBALL:   return new Sport(type, 6, 12, 17, 28, false, "Best of five sets; no draws.");
            default:           return new Sport(type, 1, 2,  17, 28, false, "Best of five games; no draws.");
        }
    }

    /**
     * Eligibility rule used by Team.addPlayer and Tournament.addRegistration.
     * Checks age band, sports-office registration and the player's declared interest.
     */
    public void checkEligibility(Player player) throws IneligiblePlayerException {
        if (player.getAge() < minAge || player.getAge() > maxAge) {
            throw new IneligiblePlayerException(player.getName(),
                    "age " + player.getAge() + " is outside the " + minAge + "-" + maxAge + " band for " + type);
        }
        if (!player.isRegistered()) {
            throw new IneligiblePlayerException(player.getName(), "not registered with the sports office");
        }
        if (!player.prefers(type)) {
            throw new IneligiblePlayerException(player.getName(), type + " is not in the player's sport preferences");
        }
    }

    public SportType getType() { return type; }
    public int getMinTeamSize() { return minTeamSize; }
    public int getMaxTeamSize() { return maxTeamSize; }
    public int getMinAge() { return minAge; }
    public int getMaxAge() { return maxAge; }
    public boolean isDrawsAllowed() { return drawsAllowed; }
    public String getRules() { return rules; }

    @Override
    public String toString() { return type.getDisplayName(); }
}
