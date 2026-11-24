package com.thapar.sportssphere.model;

import com.thapar.sportssphere.exception.InvalidOperationException;
import com.thapar.sportssphere.exception.PlayerNotFoundException;
import com.thapar.sportssphere.util.FailureLog;
import com.thapar.sportssphere.util.IdGenerator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The whole application state in one serializable object, so the repository can save and
 * load it atomically. HashMap-style lookups by id are O(1); LinkedHashMap keeps insertion
 * order so reports are stable. Collections are never handed out mutable.
 */
public class SportsData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, Player> players = new LinkedHashMap<>();
    private final Map<String, Coach> coaches = new LinkedHashMap<>();
    private final Map<String, SportsAdministrator> administrators = new LinkedHashMap<>();
    private final Map<String, Team> teams = new LinkedHashMap<>();
    private final Map<String, Tournament> tournaments = new LinkedHashMap<>();
    private final Map<String, SportsFacility> facilities = new LinkedHashMap<>();
    private final List<Match> matches = new ArrayList<>();
    private final EquipmentInventory inventory = new EquipmentInventory();
    private final FailureLog failureLog = new FailureLog();

    // ---- adding -----------------------------------------------------------------

    public void addPlayer(Player p) throws InvalidOperationException { putUnique(players, p.getId(), p, "player"); }
    public void addCoach(Coach c) throws InvalidOperationException { putUnique(coaches, c.getId(), c, "coach"); }
    public void addAdministrator(SportsAdministrator a) throws InvalidOperationException { putUnique(administrators, a.getId(), a, "administrator"); }
    public void addTeam(Team t) throws InvalidOperationException { putUnique(teams, t.getTeamId(), t, "team"); }
    public void addTournament(Tournament t) throws InvalidOperationException { putUnique(tournaments, t.getTournamentId(), t, "tournament"); }
    public void addFacility(SportsFacility f) throws InvalidOperationException { putUnique(facilities, f.getFacilityId(), f, "facility"); }
    public void addMatch(Match m) { matches.add(m); }

    private static <T> void putUnique(Map<String, T> map, String id, T value, String label)
            throws InvalidOperationException {
        if (map.containsKey(id)) {
            throw new InvalidOperationException(label + " id " + id + " already exists");
        }
        map.put(id, value);
    }

    // ---- lookups (ids are forgiving: trimmed and case-insensitive) ---------------

    private static String key(String id) { return id == null ? "" : id.trim().toUpperCase(); }

    private static <T> T require(Map<String, T> map, String id, String label) throws InvalidOperationException {
        T found = map.get(key(id));
        if (found == null) {
            throw new InvalidOperationException("unknown " + label + " id '" + id + "'");
        }
        return found;
    }

    public Player findPlayer(String id) throws PlayerNotFoundException {
        Player p = players.get(key(id));
        if (p == null) {
            throw new PlayerNotFoundException(id);
        }
        return p;
    }

    public Coach findCoach(String id) throws InvalidOperationException { return require(coaches, id, "coach"); }
    public Team findTeam(String id) throws InvalidOperationException { return require(teams, id, "team"); }
    public Tournament findTournament(String id) throws InvalidOperationException { return require(tournaments, id, "tournament"); }
    public SportsFacility findFacility(String id) throws InvalidOperationException { return require(facilities, id, "facility"); }

    public Match findMatch(String id) throws InvalidOperationException {
        for (Match m : matches) {
            if (m.getMatchId().equalsIgnoreCase(id == null ? "" : id.trim())) return m;
        }
        throw new InvalidOperationException("unknown match id '" + id + "'");
    }

    public Registration findRegistration(String id) throws InvalidOperationException {
        String wanted = id == null ? "" : id.trim();
        for (Tournament t : tournaments.values()) {
            for (Registration r : t.getRegistrations()) {
                if (r.getRegistrationId().equalsIgnoreCase(wanted)) return r;
            }
        }
        throw new InvalidOperationException("unknown registration id '" + id + "'");
    }

    // ---- read-only views --------------------------------------------------------

    public Collection<Player> getPlayers() { return Collections.unmodifiableCollection(players.values()); }
    public Collection<Coach> getCoaches() { return Collections.unmodifiableCollection(coaches.values()); }
    public Collection<SportsAdministrator> getAdministrators() { return Collections.unmodifiableCollection(administrators.values()); }
    public Collection<Team> getTeams() { return Collections.unmodifiableCollection(teams.values()); }
    public Collection<Tournament> getTournaments() { return Collections.unmodifiableCollection(tournaments.values()); }
    public Collection<SportsFacility> getFacilities() { return Collections.unmodifiableCollection(facilities.values()); }
    public List<Match> getMatches() { return Collections.unmodifiableList(matches); }
    public EquipmentInventory getInventory() { return inventory; }
    public FailureLog getFailureLog() { return failureLog; }

    /** RUNTIME POLYMORPHISM: one list of the abstract type holds every kind of person. */
    public List<Person> getAllPeople() {
        List<Person> all = new ArrayList<>();
        all.addAll(players.values());
        all.addAll(coaches.values());
        all.addAll(administrators.values());
        return all;
    }

    public boolean rollNoExists(String rollNo) {
        for (Player p : players.values()) {
            if (p.getRollNo().equalsIgnoreCase(rollNo.trim())) return true;
        }
        return false;
    }

    /** After loading a file, move the id counters past every loaded id so new ids never collide. */
    public void observeIds() {
        for (String id : players.keySet()) IdGenerator.observe(id);
        for (String id : coaches.keySet()) IdGenerator.observe(id);
        for (String id : administrators.keySet()) IdGenerator.observe(id);
        for (String id : teams.keySet()) IdGenerator.observe(id);
        for (String id : facilities.keySet()) IdGenerator.observe(id);
        for (Tournament t : tournaments.values()) {
            IdGenerator.observe(t.getTournamentId());
            for (Registration r : t.getRegistrations()) IdGenerator.observe(r.getRegistrationId());
        }
        for (Match m : matches) IdGenerator.observe(m.getMatchId());
        for (Equipment e : inventory.getItems()) IdGenerator.observe(e.getEquipmentId());
    }
}
