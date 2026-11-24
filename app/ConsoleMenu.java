package com.thapar.sportssphere.app;

import com.thapar.sportssphere.exception.SportsException;
import com.thapar.sportssphere.model.Coach;
import com.thapar.sportssphere.model.Equipment;
import com.thapar.sportssphere.model.Match;
import com.thapar.sportssphere.model.Person;
import com.thapar.sportssphere.model.Player;
import com.thapar.sportssphere.model.Registration;
import com.thapar.sportssphere.model.SportsFacility;
import com.thapar.sportssphere.model.Team;
import com.thapar.sportssphere.model.TimeSlot;
import com.thapar.sportssphere.model.Tournament;
import com.thapar.sportssphere.model.enums.EquipmentCondition;
import com.thapar.sportssphere.model.enums.SportType;
import com.thapar.sportssphere.repository.DataStore;
import com.thapar.sportssphere.service.SportsManagementSystem;
import com.thapar.sportssphere.util.InputClosedException;
import com.thapar.sportssphere.util.InputReader;
import com.thapar.sportssphere.util.OperationCancelledException;

import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Menu-driven console UI. It only collects input, calls the services and prints results.
 * Business exceptions are CAUGHT HERE, at the application boundary, in execute(): the message is
 * shown to the user and the failure is logged. The services guarantee a failed call changed nothing.
 */
public class ConsoleMenu {

    @FunctionalInterface
    private interface MenuAction {
        void run() throws SportsException;
    }

    private SportsManagementSystem sys;
    private final InputReader in;
    private final DataStore store;
    private final PrintStream out;
    private boolean unsavedChanges;

    public ConsoleMenu(SportsManagementSystem sys, InputReader in, DataStore store, PrintStream out) {
        this.sys = sys;
        this.in = in;
        this.store = store;
        this.out = out;
    }

    // =====================================================================================
    //  Main loop
    // =====================================================================================

    public void run() {
        out.println();
        out.println("==============================================================");
        out.println("  THAPAR SPORTSSPHERE - Campus Sports & Tournament Management");
        out.println("  Educational project; simulated data and payments only. Not");
        out.println("  affiliated with or connected to any official Thapar system.");
        out.println("  Tip: type 'cancel' at any prompt to go back.");
        out.println("==============================================================");
        try {
            boolean running = true;
            while (running) {
                printMainMenu();
                try {
                    running = dispatch(in.readInt("Enter choice", 0, 13));
                } catch (OperationCancelledException e) {
                    out.println("[CANCELLED] Back at the main menu.");
                }
            }
        } catch (InputClosedException e) {
            out.println();
            out.println("Input closed - exiting. (Unsaved changes are not saved automatically.)");
        }
    }

    private void printMainMenu() {
        out.println();
        out.println("------------------------- MAIN MENU -------------------------");
        String[][] rows = {
            {"1. Player management", "8. Match scheduling"},
            {"2. Coach management", "9. Score entry and results"},
            {"3. Team and roster management", "10. Performance analytics"},
            {"4. Tournament management", "11. Reports"},
            {"5. Tournament registration", "12. Save data"},
            {"6. Facility availability/booking", "13. Load data"},
            {"7. Equipment issue and return", "0. Exit"},
        };
        for (String[] r : rows) {
            out.println(String.format("  %-34s %s", r[0], r[1]));
        }
        out.println("--------------------------------------------------------------");
    }

    private boolean dispatch(int choice) {
        switch (choice) {
            case 1:  playerMenu(); break;
            case 2:  coachMenu(); break;
            case 3:  teamMenu(); break;
            case 4:  tournamentMenu(); break;
            case 5:  registrationMenu(); break;
            case 6:  facilityMenu(); break;
            case 7:  equipmentMenu(); break;
            case 8:  matchSchedulingMenu(); break;
            case 9:  scoreMenu(); break;
            case 10: analyticsMenu(); break;
            case 11: reportsMenu(); break;
            case 12: execute("Save data", false, this::saveData); break;
            case 13: execute("Load data", false, this::loadData); break;
            default: return !confirmExit();
        }
        return true;
    }

    /**
     * The single place where business failures become user messages.
     * Only the exception families we expect are caught; anything else is a real bug and must surface.
     */
    private void execute(String operation, boolean changesData, MenuAction action) {
        try {
            action.run();
            if (changesData) unsavedChanges = true;
        } catch (OperationCancelledException e) {
            out.println("[CANCELLED] Nothing was changed.");
        } catch (SportsException e) {
            out.println("[REJECTED] " + e.getMessage());
            sys.logFailure(operation, e);
        } catch (IllegalArgumentException e) {
            // Thrown by model validation (bad age, blank name, ...) - input the model refused.
            out.println("[INVALID INPUT] " + e.getMessage());
            sys.logFailure(operation, e);
        }
    }

    /** Returns true when the user really wants to leave. Offers to save unsaved work first. */
    private boolean confirmExit() {
        try {
            if (unsavedChanges && in.readYesNo("You have unsaved changes. Save before exiting?")) {
                execute("Save data", false, this::saveData);
                if (unsavedChanges && !in.readYesNo("Saving failed. Exit anyway?")) {
                    return false;
                }
            }
        } catch (OperationCancelledException e) {
            out.println("[CANCELLED] Staying in the program.");
            return false;
        }
        out.println("Goodbye!");
        return true;
    }

    // =====================================================================================
    //  1. Players
    // =====================================================================================

    private void playerMenu() {
        out.println("\n-- Player management --");
        out.println("1. Add player   2. List players   3. View one player   4. Register player with sports office");
        out.println("5. View everyone (players, coaches, administrators)   6. Add administrator");
        switch (in.readInt("Choose", 1, 6)) {
            case 1: execute("Add player", true, this::addPlayer); break;
            case 2: execute("List players", false, () -> out.print(sys.reports().playerProfiles())); break;
            case 3: execute("View player", false, () -> { listPlayers(); sys.people().findPlayer(in.readRequired("Player id")).displayDetails(); }); break;
            case 4: execute("Register with sports office", true, this::registerWithOffice); break;
            case 5: execute("View everyone", false, this::viewEveryone); break;
            default: execute("Add administrator", true, this::addAdministrator); break;
        }
    }

    private void addPlayer() throws SportsException {
        String name = in.readRequired("Name");
        int age = in.readInt("Age");
        String contact = in.readOptional("Contact (optional)");
        String roll = in.readRequired("Roll number");
        Set<SportType> prefs = EnumSet.noneOf(SportType.class);
        do {
            prefs.add(in.readEnum("Sport preference", SportType.values()));
        } while (in.readYesNo("Add another sport preference?"));
        boolean registered = in.readYesNo("Already registered with the sports office?");
        Player p = sys.people().addPlayer(name, age, contact, roll, prefs, registered);
        out.println("Added player " + p.getId() + " - " + p.getName());
    }

    private void registerWithOffice() throws SportsException {
        listPlayers();
        Player p = sys.people().findPlayer(in.readRequired("Player id"));
        sys.people().registerWithSportsOffice(p.getId());
        out.println(p.getName() + " is now registered with the sports office.");
    }

    /** Runtime polymorphism in the live app: each Person subclass prints its own profile. */
    private void viewEveryone() {
        List<Person> everyone = sys.getData().getAllPeople();
        if (everyone.isEmpty()) out.println("(nobody added yet)");
        for (Person p : everyone) {
            p.displayDetails();
        }
        out.print(sys.reports().peopleDirectory());
    }

    private void addAdministrator() throws SportsException {
        String name = in.readRequired("Name");
        int age = in.readInt("Age");
        String contact = in.readOptional("Contact (optional)");
        String employeeId = in.readRequired("Employee id");
        out.println("Added administrator " + sys.people().addAdministrator(name, age, contact, employeeId).getId());
    }

    // =====================================================================================
    //  2. Coaches
    // =====================================================================================

    private void coachMenu() {
        out.println("\n-- Coach management --");
        out.println("1. Add coach   2. List coaches   3. Assign coach to team");
        switch (in.readInt("Choose", 1, 3)) {
            case 1: execute("Add coach", true, this::addCoach); break;
            case 2: execute("List coaches", false, () -> {
                if (sys.getData().getCoaches().isEmpty()) out.println("(no coaches)");
                for (Coach c : sys.getData().getCoaches()) c.displayDetails();
            }); break;
            default: execute("Assign coach", true, () -> {
                listCoaches();
                String coachId = in.readRequired("Coach id");
                listTeams();
                String teamId = in.readRequired("Team id");
                sys.people().assignCoach(coachId, teamId);
                out.println("Coach assigned.");
            }); break;
        }
    }

    private void addCoach() throws SportsException {
        String name = in.readRequired("Name");
        int age = in.readInt("Age");
        String contact = in.readOptional("Contact (optional)");
        SportType type = in.readEnum("Specialization", SportType.values());
        out.println("Added coach " + sys.people().addCoach(name, age, contact, type).getId());
    }

    // =====================================================================================
    //  3. Teams
    // =====================================================================================

    private void teamMenu() {
        out.println("\n-- Team creation and roster management --");
        out.println("1. Create team   2. Add player to team   3. Remove player from team   4. Set captain   5. View rosters");
        switch (in.readInt("Choose", 1, 5)) {
            case 1: execute("Create team", true, () -> {
                String name = in.readRequired("Team name");
                SportType type = in.readEnum("Sport", SportType.values());
                out.println("Created team " + sys.people().createTeam(name, type).getTeamId());
            }); break;
            case 2: execute("Add player to team", true, () -> {
                listTeams(); String teamId = in.readRequired("Team id");
                listPlayers(); String playerId = in.readRequired("Player id");
                sys.people().addPlayerToTeam(teamId, playerId);
                out.println("Player added to the team.");
            }); break;
            case 3: execute("Remove player from team", true, () -> {
                listTeams(); String teamId = in.readRequired("Team id");
                String playerId = in.readRequired("Player id");
                sys.people().removePlayerFromTeam(teamId, playerId);
                out.println("Player removed from the team.");
            }); break;
            case 4: execute("Set captain", true, () -> {
                listTeams(); String teamId = in.readRequired("Team id");
                String playerId = in.readRequired("Player id of the captain");
                sys.people().setCaptain(teamId, playerId);
                out.println("Captain set.");
            }); break;
            default: execute("View rosters", false, () -> out.print(sys.reports().teamRosters())); break;
        }
    }

    // =====================================================================================
    //  4. Tournaments
    // =====================================================================================

    private void tournamentMenu() {
        out.println("\n-- Tournament management --");
        out.println("1. Create   2. Open registration   3. Close registration   4. Start   5. Complete   6. List");
        switch (in.readInt("Choose", 1, 6)) {
            case 1: execute("Create tournament", true, this::createTournament); break;
            case 2: execute("Open registration", true, () -> { listTournaments(); sys.registrations().openRegistration(in.readRequired("Tournament id")); out.println("Registration is now open."); }); break;
            case 3: execute("Close registration", true, () -> { listTournaments(); sys.registrations().closeRegistration(in.readRequired("Tournament id")); out.println("Registration closed."); }); break;
            case 4: execute("Start tournament", true, () -> { listTournaments(); sys.registrations().startTournament(in.readRequired("Tournament id")); out.println("Tournament started."); }); break;
            case 5: execute("Complete tournament", true, () -> { listTournaments(); sys.registrations().completeTournament(in.readRequired("Tournament id")); out.println("Tournament completed."); }); break;
            default: execute("List tournaments", false, () -> out.print(sys.reports().tournamentRegistrations())); break;
        }
    }

    private void createTournament() throws SportsException {
        String name = in.readRequired("Tournament name");
        SportType type = in.readEnum("Sport", SportType.values());
        LocalDate deadline = in.readDate("Registration deadline");
        int max = in.readInt("Maximum entries (teams or individual players)");
        double multiplier = in.readDouble("Fee multiplier (1.0 = normal fee)", 0, 10);
        Tournament t = sys.registrations().createTournament(name, type, deadline, max, multiplier);
        out.println("Created tournament " + t.getTournamentId() + " (status DRAFT).");
    }

    // =====================================================================================
    //  5. Registration and payment
    // =====================================================================================

    private void registrationMenu() {
        out.println("\n-- Tournament registration (payments are SIMULATED) --");
        out.println("1. Register player + pay   2. Register team + pay   3. Reserve player slot (pay later)");
        out.println("4. Reserve team slot (pay later)   5. Pay a pending registration   6. Cancel registration   7. View registrations");
        switch (in.readInt("Choose", 1, 7)) {
            case 1: execute("Register player and pay", true, () -> {
                listTournaments(); String tid = in.readRequired("Tournament id");
                listPlayers(); String pid = in.readRequired("Player id");
                Registration r = sys.registrations().registerPlayerAndPay(tid, pid, askPaymentOutcome());
                out.println("Registered and paid.\n" + sys.payments().receipt(r));
            }); break;
            case 2: execute("Register team and pay", true, () -> {
                listTournaments(); String tid = in.readRequired("Tournament id");
                listTeams(); String teamId = in.readRequired("Team id");
                Registration r = sys.registrations().registerTeamAndPay(tid, teamId, askPaymentOutcome());
                out.println("Registered and paid.\n" + sys.payments().receipt(r));
            }); break;
            case 3: execute("Reserve player slot", true, () -> {
                listTournaments(); String tid = in.readRequired("Tournament id");
                listPlayers(); String pid = in.readRequired("Player id");
                Registration r = sys.registrations().registerPlayer(tid, pid);
                out.println("Slot reserved: " + r.getRegistrationId() + " (PENDING until paid, fee Rs " + String.format("%.2f", r.getPayment().getAmount()) + ").");
            }); break;
            case 4: execute("Reserve team slot", true, () -> {
                listTournaments(); String tid = in.readRequired("Tournament id");
                listTeams(); String teamId = in.readRequired("Team id");
                Registration r = sys.registrations().registerTeam(tid, teamId);
                out.println("Slot reserved: " + r.getRegistrationId() + " (PENDING until paid, fee Rs " + String.format("%.2f", r.getPayment().getAmount()) + ").");
            }); break;
            case 5: execute("Pay pending registration", true, () -> {
                out.print(sys.reports().tournamentRegistrations());
                Registration r = sys.payments().payPending(in.readRequired("Registration id"), askPaymentOutcome());
                out.println("Payment completed.\n" + sys.payments().receipt(r));
            }); break;
            case 6: execute("Cancel registration", true, () -> {
                out.print(sys.reports().tournamentRegistrations());
                sys.registrations().cancelRegistration(in.readRequired("Registration id"));
                out.println("Registration cancelled; the slot is free again.");
            }); break;
            default: execute("View registrations", false, () -> out.print(sys.reports().tournamentRegistrations())); break;
        }
    }

    private boolean askPaymentOutcome() {
        return in.readYesNo("SIMULATION: should the pretend payment gateway approve this payment?");
    }

    // =====================================================================================
    //  6. Facilities
    // =====================================================================================

    private void facilityMenu() {
        out.println("\n-- Facility availability and booking --");
        out.println("1. Add facility   2. View free slots   3. Book facility   4. Cancel booking   5. View booking schedules");
        switch (in.readInt("Choose", 1, 5)) {
            case 1: execute("Add facility", true, this::addFacility); break;
            case 2: execute("View free slots", false, () -> {
                listFacilities(); String fid = in.readRequired("Facility id");
                LocalDate date = in.readDate("Date");
                List<TimeSlot> free = sys.bookings().freeSlots(fid, date);
                if (free.isEmpty()) out.println("Fully booked on that date.");
                for (TimeSlot s : free) out.println("   free: " + s + " (" + s.minutes() + " min)");
            }); break;
            case 3: execute("Book facility", true, () -> {
                listFacilities(); String fid = in.readRequired("Facility id");
                LocalDate date = in.readDate("Date");
                LocalTime from = in.readTime("Start time");
                LocalTime to = in.readTime("End time");
                String purpose = in.readRequired("Purpose");
                String by = in.readRequired("Booked by");
                out.println("Booked: " + sys.bookings().book(fid, date.atTime(from), date.atTime(to), purpose, by));
            }); break;
            case 4: execute("Cancel booking", true, () -> {
                out.print(sys.reports().facilitySchedules());
                String fid = in.readRequired("Facility id");
                sys.bookings().cancel(fid, in.readRequired("Booking id"));
                out.println("Booking cancelled.");
            }); break;
            default: execute("View schedules", false, () -> out.print(sys.reports().facilitySchedules())); break;
        }
    }

    private void addFacility() throws SportsException {
        String name = in.readRequired("Facility name");
        SportType type = in.readEnum("Sport the facility is for", SportType.values());
        SportsFacility f;
        if (in.readYesNo("Use default opening hours 06:00-22:00?")) {
            f = sys.bookings().addFacility(name, type);
        } else {
            LocalTime opens = in.readTime("Opens at");
            LocalTime closes = in.readTime("Closes at");
            f = sys.bookings().addFacility(name, type, opens, closes);
        }
        out.println("Added facility " + f.getFacilityId());
    }

    // =====================================================================================
    //  7. Equipment
    // =====================================================================================

    private void equipmentMenu() {
        out.println("\n-- Equipment issue and return --");
        out.println("1. Add equipment   2. Issue   3. Return   4. View availability");
        switch (in.readInt("Choose", 1, 4)) {
            case 1: execute("Add equipment", true, () -> {
                String name = in.readRequired("Equipment name");
                int stock = in.readInt("Stock quantity");
                EquipmentCondition c = in.readEnum("Condition", EquipmentCondition.values());
                out.println("Added " + sys.equipment().addEquipment(name, stock, c).getEquipmentId());
            }); break;
            case 2: execute("Issue equipment", true, () -> {
                out.print(sys.reports().equipmentAvailability());
                String eid = in.readRequired("Equipment id");
                listTeams(); listPlayers();
                String holder = in.readRequired("Issue to (team id or player id)");
                int qty = in.readInt("Quantity");
                sys.equipment().issue(eid, holder, qty);
                out.println("Issued " + qty + " item(s).");
            }); break;
            case 3: execute("Return equipment", true, () -> {
                out.print(sys.reports().equipmentAvailability());
                String eid = in.readRequired("Equipment id");
                String holder = in.readRequired("Returned by (team id or player id)");
                int qty = in.readInt("Quantity returned");
                EquipmentCondition c = in.readEnum("Condition of returned items", EquipmentCondition.values());
                sys.equipment().returnItem(eid, holder, qty, c);
                out.println("Returned " + qty + " item(s); stock restored.");
            }); break;
            default: execute("View equipment", false, () -> out.print(sys.reports().equipmentAvailability())); break;
        }
    }

    // =====================================================================================
    //  8. Matches and 9. Scores
    // =====================================================================================

    private void matchSchedulingMenu() {
        out.println("\n-- Match scheduling --");
        out.println("1. Schedule match   2. Cancel match   3. View fixtures");
        switch (in.readInt("Choose", 1, 3)) {
            case 1: execute("Schedule match", true, () -> {
                listTournaments(); String tid = in.readRequired("Tournament id");
                listTeams();
                String a = in.readRequired("Team A id");
                String b = in.readRequired("Team B id");
                listFacilities(); String fid = in.readRequired("Facility id");
                LocalDate date = in.readDate("Match date");
                LocalTime time = in.readTime("Start time");
                Match m = sys.matches().schedule(tid, a, b, fid, LocalDateTime.of(date, time));
                out.println("Scheduled: " + m);
            }); break;
            case 2: execute("Cancel match", true, () -> {
                out.print(sys.reports().fixturesAndResults());
                sys.matches().cancelMatch(in.readRequired("Match id"));
                out.println("Match cancelled and venue released.");
            }); break;
            default: execute("View fixtures", false, () -> out.print(sys.reports().fixturesAndResults())); break;
        }
    }

    private void scoreMenu() {
        out.println("\n-- Score entry and results --");
        out.println("1. Enter score   2. Correct a recorded score   3. View fixtures and results");
        switch (in.readInt("Choose", 1, 3)) {
            case 1: execute("Enter score", true, () -> {
                out.print(sys.reports().fixturesAndResults());
                String mid = in.readRequired("Match id");
                // Parsed as plain integers on purpose: Match.recordScore owns the rule that scores cannot be negative.
                int a = in.readInt("Score of Team A");
                int b = in.readInt("Score of Team B");
                sys.matches().enterScore(mid, a, b);
                out.println("Result recorded.");
            }); break;
            case 2: execute("Correct score", true, () -> {
                out.print(sys.reports().fixturesAndResults());
                String mid = in.readRequired("Match id");
                int a = in.readInt("Corrected score of Team A");
                int b = in.readInt("Corrected score of Team B");
                String reason = in.readRequired("Reason for correction");
                sys.matches().correctScore(mid, a, b, reason);
                out.println("Result corrected and player statistics rebuilt.");
            }); break;
            default: execute("View results", false, () -> out.print(sys.reports().fixturesAndResults())); break;
        }
    }

    // =====================================================================================
    //  10. Analytics and 11. Reports
    // =====================================================================================

    private void analyticsMenu() {
        out.println("\n-- Performance analytics --");
        out.println("1. Tournament standings   2. Top players   3. Tournament summary   4. Player statistics");
        switch (in.readInt("Choose", 1, 4)) {
            case 1: execute("Standings", false, () -> { listTournaments(); out.print(sys.reports().standings(in.readRequired("Tournament id"))); }); break;
            case 2: execute("Top players", false, () -> out.print(sys.reports().topPlayers(in.readInt("How many players", 1, 50)))); break;
            case 3: execute("Tournament summary", false, () -> { listTournaments(); out.print(sys.reports().tournamentSummary(in.readRequired("Tournament id"))); }); break;
            default: execute("Player statistics", false, () -> { listPlayers(); out.print(sys.reports().playerProfile(sys.people().findPlayer(in.readRequired("Player id")))); }); break;
        }
    }

    private void reportsMenu() {
        out.println("\n-- Reports --");
        out.println("1. Player profiles   2. Team rosters   3. Tournament registrations   4. Facility booking schedules");
        out.println("5. Equipment availability   6. Match fixtures and results   7. Tournament standings");
        out.println("8. Failed operations   9. People directory");
        switch (in.readInt("Choose", 1, 9)) {
            case 1: execute("Report: players", false, () -> out.print(sys.reports().playerProfiles())); break;
            case 2: execute("Report: rosters", false, () -> out.print(sys.reports().teamRosters())); break;
            case 3: execute("Report: registrations", false, () -> out.print(sys.reports().tournamentRegistrations())); break;
            case 4: execute("Report: facility schedules", false, () -> out.print(sys.reports().facilitySchedules())); break;
            case 5: execute("Report: equipment", false, () -> out.print(sys.reports().equipmentAvailability())); break;
            case 6: execute("Report: fixtures", false, () -> out.print(sys.reports().fixturesAndResults())); break;
            case 7: execute("Report: standings", false, () -> { listTournaments(); out.print(sys.reports().standings(in.readRequired("Tournament id"))); }); break;
            case 8: execute("Report: failures", false, () -> out.print(sys.reports().failedOperations())); break;
            default: execute("Report: people", false, () -> out.print(sys.reports().peopleDirectory())); break;
        }
    }

    // =====================================================================================
    //  12. Save and 13. Load
    // =====================================================================================

    private void saveData() throws SportsException {
        store.save(sys.getData());
        unsavedChanges = false;
        out.println("Data saved.");
    }

    private void loadData() throws SportsException {
        if (unsavedChanges && !in.readYesNo("Unsaved changes will be replaced by the saved file. Continue?")) {
            out.println("Load cancelled.");
            return;
        }
        // Build the new system first; the current one is replaced only if loading succeeds.
        SportsManagementSystem loaded = new SportsManagementSystem(store.load(), sys.getClock());
        sys = loaded;
        unsavedChanges = false;
        out.println("Data loaded: " + sys.getData().getPlayers().size() + " players, "
                + sys.getData().getTeams().size() + " teams, " + sys.getData().getTournaments().size() + " tournaments.");
    }

    // =====================================================================================
    //  Small listing helpers so the user can pick ids without remembering them
    // =====================================================================================

    private void listPlayers() {
        out.println("Players:");
        if (sys.getData().getPlayers().isEmpty()) out.println("   (none)");
        for (Player p : sys.getData().getPlayers()) out.println("   " + p.getId() + "  " + p.getName() + "  " + p.getRollNo());
    }

    private void listCoaches() {
        out.println("Coaches:");
        if (sys.getData().getCoaches().isEmpty()) out.println("   (none)");
        for (Coach c : sys.getData().getCoaches()) out.println("   " + c.getId() + "  " + c.getName() + "  " + c.getSpecialization());
    }

    private void listTeams() {
        out.println("Teams:");
        if (sys.getData().getTeams().isEmpty()) out.println("   (none)");
        for (Team t : sys.getData().getTeams()) out.println("   " + t.getTeamId() + "  " + t.getName() + "  " + t.getSport() + "  (" + t.size() + " players)");
    }

    private void listTournaments() {
        out.println("Tournaments:");
        if (sys.getData().getTournaments().isEmpty()) out.println("   (none)");
        for (Tournament t : sys.getData().getTournaments()) out.println("   " + t.getTournamentId() + "  " + t.getName() + "  " + t.getSport() + "  " + t.getStatus());
    }

    private void listFacilities() {
        out.println("Facilities:");
        if (sys.getData().getFacilities().isEmpty()) out.println("   (none)");
        for (SportsFacility f : sys.getData().getFacilities()) out.println("   " + f.getFacilityId() + "  " + f);
    }
}
