package com.thapar.sportssphere.model;

import com.thapar.sportssphere.exception.InvalidOperationException;
import com.thapar.sportssphere.model.enums.SportType;
import com.thapar.sportssphere.util.Validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A coach with one specialization and a limited number of assigned teams (association). */
public class Coach extends Person {
    private static final long serialVersionUID = 1L;

    public static final int MAX_TEAMS = 3;

    private final SportType specialization;
    private final List<Team> teams = new ArrayList<>();

    public Coach(String id, String name, int age, String contact, SportType specialization) {
        super(id, name, age, contact);
        this.specialization = Validator.requireNonNull(specialization, "Specialization");
    }

    public Coach(String id, String name, int age, SportType specialization) {
        this(id, name, age, "N/A", specialization);
    }

    public SportType getSpecialization() { return specialization; }

    public List<Team> getTeams() { return Collections.unmodifiableList(teams); }

    public void assignTeam(Team team) throws InvalidOperationException {
        Validator.requireNonNull(team, "Team");
        if (team.getSport().getType() != specialization) {
            throw new InvalidOperationException(getName() + " specializes in " + specialization
                    + " and cannot coach a " + team.getSport().getType() + " team.");
        }
        if (teams.contains(team)) {
            throw new InvalidOperationException(getName() + " already coaches " + team.getName() + ".");
        }
        if (teams.size() >= MAX_TEAMS) {
            throw new InvalidOperationException(getName() + " already coaches the maximum of " + MAX_TEAMS + " teams.");
        }
        teams.add(team);
    }

    @Override
    public double calculateFee() { return 0.0; }   // coaches do not pay entry fees

    @Override
    public void displayDetails() {
        System.out.println("[COACH] " + baseDetails());
        System.out.println("   Specialization: " + specialization + " | Teams: " + teams.size());
        for (Team t : teams) {
            System.out.println("     - " + t.getName());
        }
    }
}
