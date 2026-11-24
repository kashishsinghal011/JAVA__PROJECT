package com.thapar.sportssphere.model;

import com.thapar.sportssphere.exception.IneligiblePlayerException;
import com.thapar.sportssphere.exception.InvalidOperationException;
import com.thapar.sportssphere.exception.PlayerNotFoundException;
import com.thapar.sportssphere.exception.TeamCapacityExceededException;
import com.thapar.sportssphere.model.contract.Reportable;
import com.thapar.sportssphere.util.Validator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Roster of players for one sport. AGGREGATION: players exist independently of the team.
 * The roster list is private and only exposed as an unmodifiable view.
 */
public class Team implements Serializable, Reportable {
    private static final long serialVersionUID = 1L;

    private final String teamId;
    private String name;
    private final Sport sport;
    private Player captain;
    private final List<Player> roster = new ArrayList<>();

    public Team(String teamId, String name, Sport sport) {
        this.teamId = Validator.requireNonBlank(teamId, "Team id");
        this.name = Validator.requireNonBlank(name, "Team name");
        this.sport = Validator.requireNonNull(sport, "Sport");
    }

    /**
     * All checks run before the roster changes, so a failed call leaves the team untouched.
     */
    public void addPlayer(Player player)
            throws TeamCapacityExceededException, IneligiblePlayerException, InvalidOperationException {
        Validator.requireNonNull(player, "Player");
        if (hasPlayer(player.getId())) {
            throw new InvalidOperationException(player.getName() + " is already in " + name + ".");
        }
        sport.checkEligibility(player);
        if (roster.size() >= sport.getMaxTeamSize()) {
            throw new TeamCapacityExceededException(name, sport.getMaxTeamSize());
        }
        roster.add(player);
    }

    public void removePlayer(String playerId) throws PlayerNotFoundException {
        boolean removed = roster.removeIf(p -> p.getId().equals(playerId));
        if (!removed) {
            throw new PlayerNotFoundException(playerId);
        }
        if (captain != null && captain.getId().equals(playerId)) {
            captain = null;   // a team must never keep a captain who left
        }
    }

    public void setCaptain(Player player) throws InvalidOperationException {
        Validator.requireNonNull(player, "Captain");
        if (!hasPlayer(player.getId())) {
            throw new InvalidOperationException(player.getName() + " is not in " + name + " and cannot be captain.");
        }
        this.captain = player;
    }

    public boolean hasPlayer(String playerId) {
        for (Player p : roster) {
            if (p.getId().equals(playerId)) return true;
        }
        return false;
    }

    /** True when the roster meets the sport's minimum size. */
    public boolean isMatchReady() { return roster.size() >= sport.getMinTeamSize(); }

    public List<Player> getRoster() { return Collections.unmodifiableList(roster); }
    public int size() { return roster.size(); }
    public String getTeamId() { return teamId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = Validator.requireNonBlank(name, "Team name"); }
    public Sport getSport() { return sport; }
    public Player getCaptain() { return captain; }

    @Override
    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("TEAM %s (%s) - %s | Players %d/%d | Captain: %s%n",
                teamId, name, sport, roster.size(), sport.getMaxTeamSize(),
                captain == null ? "not assigned" : captain.getName()));
        if (roster.isEmpty()) {
            sb.append("   (empty roster)\n");
        }
        for (Player p : roster) {
            sb.append(String.format("   - %-8s %-20s %s%n", p.getId(), p.getName(), p.getRollNo()));
        }
        return sb.toString();
    }

    @Override
    public String toString() { return name; }
}
