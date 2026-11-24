package com.thapar.sportssphere.model.enums;

/** Result of a match from one team's point of view. */
public enum MatchOutcome {
    WIN, LOSS, DRAW;

    /** The same match seen from the other team's side. */
    public MatchOutcome opposite() {
        switch (this) {
            case WIN:  return LOSS;
            case LOSS: return WIN;
            default:   return DRAW;
        }
    }
}
