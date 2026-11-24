package com.thapar.sportssphere.service;

import com.thapar.sportssphere.exception.IneligiblePlayerException;
import com.thapar.sportssphere.exception.InvalidOperationException;
import com.thapar.sportssphere.exception.PlayerNotFoundException;
import com.thapar.sportssphere.exception.TeamCapacityExceededException;
import com.thapar.sportssphere.model.Coach;
import com.thapar.sportssphere.model.Player;
import com.thapar.sportssphere.model.SportsAdministrator;
import com.thapar.sportssphere.model.SportsData;
import com.thapar.sportssphere.model.Sport;
import com.thapar.sportssphere.model.Team;
import com.thapar.sportssphere.model.enums.SportType;
import com.thapar.sportssphere.util.IdGenerator;

import java.util.Collection;

/** Players, coaches, administrators, teams and rosters. */
public class PlayerTeamService {
    private final SportsData data;

    public PlayerTeamService(SportsData data) { this.data = data; }

    public Player addPlayer(String name, int age, String contact, String rollNo,
                            Collection<SportType> preferences, boolean officeRegistered)
            throws InvalidOperationException {
        if (rollNo != null && data.rollNoExists(rollNo)) {
            throw new InvalidOperationException("roll number " + rollNo.trim() + " already belongs to another player");
        }
        Player p = new Player(IdGenerator.next("PLR"), name, age, contact, rollNo, preferences, officeRegistered);
        data.addPlayer(p);
        return p;
    }

    public Coach addCoach(String name, int age, String contact, SportType specialization)
            throws InvalidOperationException {
        Coach c = new Coach(IdGenerator.next("CCH"), name, age, contact, specialization);
        data.addCoach(c);
        return c;
    }

    public SportsAdministrator addAdministrator(String name, int age, String contact, String employeeId)
            throws InvalidOperationException {
        SportsAdministrator a = new SportsAdministrator(IdGenerator.next("ADM"), name, age, contact, employeeId);
        data.addAdministrator(a);
        return a;
    }

    public Team createTeam(String name, SportType type) throws InvalidOperationException {
        for (Team t : data.getTeams()) {
            if (t.getName().equalsIgnoreCase(name == null ? "" : name.trim())) {
                throw new InvalidOperationException("a team named '" + t.getName() + "' already exists");
            }
        }
        Team team = new Team(IdGenerator.next("TEM"), name, Sport.standard(type));
        data.addTeam(team);
        return team;
    }

    public void addPlayerToTeam(String teamId, String playerId)
            throws PlayerNotFoundException, TeamCapacityExceededException,
                   IneligiblePlayerException, InvalidOperationException {
        Team team = data.findTeam(teamId);
        Player player = data.findPlayer(playerId);
        team.addPlayer(player);                 // Team enforces duplicate, eligibility and capacity
    }

    public void removePlayerFromTeam(String teamId, String playerId)
            throws PlayerNotFoundException, InvalidOperationException {
        data.findTeam(teamId).removePlayer(data.findPlayer(playerId).getId());
    }

    public void setCaptain(String teamId, String playerId)
            throws PlayerNotFoundException, InvalidOperationException {
        data.findTeam(teamId).setCaptain(data.findPlayer(playerId));
    }

    public void assignCoach(String coachId, String teamId) throws InvalidOperationException {
        data.findCoach(coachId).assignTeam(data.findTeam(teamId));
    }

    public void registerWithSportsOffice(String playerId) throws PlayerNotFoundException {
        data.findPlayer(playerId).markRegistered();
    }

    public Player findPlayer(String playerId) throws PlayerNotFoundException { return data.findPlayer(playerId); }
}
