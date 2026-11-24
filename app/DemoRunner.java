package com.thapar.sportssphere.app;

import com.thapar.sportssphere.exception.DataPersistenceException;
import com.thapar.sportssphere.exception.DuplicateRegistrationException;
import com.thapar.sportssphere.exception.EquipmentNotIssuedException;
import com.thapar.sportssphere.exception.FacilityUnavailableException;
import com.thapar.sportssphere.exception.IneligiblePlayerException;
import com.thapar.sportssphere.exception.InsufficientEquipmentException;
import com.thapar.sportssphere.exception.InvalidBookingException;
import com.thapar.sportssphere.exception.InvalidOperationException;
import com.thapar.sportssphere.exception.InvalidScoreException;
import com.thapar.sportssphere.exception.PaymentFailedException;
import com.thapar.sportssphere.exception.PlayerNotFoundException;
import com.thapar.sportssphere.exception.SportsException;
import com.thapar.sportssphere.exception.TeamCapacityExceededException;
import com.thapar.sportssphere.exception.TournamentFullException;
import com.thapar.sportssphere.model.Equipment;
import com.thapar.sportssphere.model.Registration;
import com.thapar.sportssphere.model.enums.EquipmentCondition;
import com.thapar.sportssphere.model.enums.MatchStatus;
import com.thapar.sportssphere.model.enums.RegistrationStatus;
import com.thapar.sportssphere.model.enums.PaymentStatus;
import com.thapar.sportssphere.repository.FileDataStore;
import com.thapar.sportssphere.service.SportsManagementSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Non-interactive walk through the 12 required scenarios plus extra cases that trigger the
 * remaining exceptions. Every scenario really calls the services; nothing is faked. Run with:
 *   java -cp out com.thapar.sportssphere.app.DemoRunner
 * Exit code is 0 only if every check passed.
 */
public class DemoRunner {

    @FunctionalInterface
    private interface Action {
        void run() throws Exception;
    }

    @FunctionalInterface
    private interface Step {
        String run() throws Exception;
    }

    private SportsManagementSystem sys;
    private int passed;
    private int failed;

    public static void main(String[] args) throws Exception {
        System.exit(new DemoRunner().runAll() ? 0 : 1);
    }

    private boolean runAll() throws Exception {
        sys = new SportsManagementSystem();
        SampleDataLoader.load(sys);
        LocalDate today = LocalDate.now(sys.getClock());
        System.out.println("THAPAR SPORTSSPHERE - DEMO (sample data loaded, simulated payments only)");
        System.out.println("Date used for relative dates: " + today);

        header(1, "Register a player successfully");
        succeed("Register Tanya Walia (PLR-017) in Badminton Open (TRN-002); simulated payment approved", () -> {
            Registration r = sys.registrations().registerPlayerAndPay("TRN-002", "PLR-017", true);
            return r.getRegistrationId() + " is " + r.getStatus() + ", fee Rs " + String.format("%.2f", r.getPayment().getAmount());
        });
        check("TRN-002 now has 1 entry", sys.getData().findTournament("TRN-002").activeEntryCount() == 1);

        header(2, "Duplicate registration");
        reject(DuplicateRegistrationException.class, "Register PLR-017 in TRN-002 again",
                () -> sys.registrations().registerPlayerAndPay("TRN-002", "PLR-017", true));
        check("TRN-002 still has 1 entry", sys.getData().findTournament("TRN-002").activeEntryCount() == 1);

        header(3, "Team capacity (Smash Squad, badminton, maximum 4)");
        succeed("Add Saanvi Bedi (PLR-016) to Smash Squad (TEM-003), 3 -> 4 players", () -> {
            sys.people().addPlayerToTeam("TEM-003", "PLR-016");
            return "roster size " + sys.getData().findTeam("TEM-003").size();
        });
        reject(TeamCapacityExceededException.class, "Add Simran Kaur (PLR-018) as a 5th player",
                () -> sys.people().addPlayerToTeam("TEM-003", "PLR-018"));
        check("roster still 4 and PLR-018 not in it", sys.getData().findTeam("TEM-003").size() == 4
                && !sys.getData().findTeam("TEM-003").hasPlayer("PLR-018"));

        header(4, "Facility booking overlap (Basketball Court A)");
        LocalDate day = today.plusDays(10);
        succeed("Book FAC-001 on " + day + " 10:00-11:00 for 'Fitness session'", () ->
                sys.bookings().book("FAC-001", day.atTime(10, 0), day.atTime(11, 0), "Fitness session", "CCH-001").toString());
        int before = sys.getData().findFacility("FAC-001").getBookings().size();
        reject(FacilityUnavailableException.class, "Book FAC-001 on " + day + " 10:30-11:30",
                () -> sys.bookings().book("FAC-001", day.atTime(10, 30), day.atTime(11, 30), "Skills camp", "CCH-001"));
        check("booking list unchanged (" + before + " bookings)", sys.getData().findFacility("FAC-001").getBookings().size() == before);
        succeed("Back-to-back booking 11:00-12:00 is allowed (intervals do not overlap)", () ->
                sys.bookings().book("FAC-001", day.atTime(11, 0), day.atTime(12, 0), "Skills camp", "CCH-001").toString());

        header(5, "Equipment issue and insufficient stock");
        succeed("Issue 12 shuttlecock tubes (EQP-002) to Smash Squad (TEM-003)", () -> {
            sys.equipment().issue("EQP-002", "TEM-003", 12);
            return "available " + stock("EQP-002").getAvailable() + "/" + stock("EQP-002").getTotalStock();
        });
        reject(InsufficientEquipmentException.class, "Issue 10 more tubes to Net Ninjas (TEM-004)",
                () -> sys.equipment().issue("EQP-002", "TEM-004", 10));
        check("available stock still 8", stock("EQP-002").getAvailable() == 8);

        header(6, "Return equipment and verify stock");
        succeed("Smash Squad returns 12 tubes, condition WORN", () -> {
            sys.equipment().returnItem("EQP-002", "TEM-003", 12, EquipmentCondition.WORN);
            return "available " + stock("EQP-002").getAvailable() + "/" + stock("EQP-002").getTotalStock()
                    + ", condition " + stock("EQP-002").getCondition();
        });
        check("stock restored to 20", stock("EQP-002").getAvailable() == 20);

        header(7, "Ineligible players");
        reject(IneligiblePlayerException.class, "Register Rahul Yadav (PLR-021, age 16) in TRN-002",
                () -> sys.registrations().registerPlayer("TRN-002", "PLR-021"));
        reject(IneligiblePlayerException.class, "Register Neha Tiwari (PLR-022, not registered with sports office) in TRN-002",
                () -> sys.registrations().registerPlayer("TRN-002", "PLR-022"));
        check("TRN-002 still has 1 entry", sys.getData().findTournament("TRN-002").activeEntryCount() == 1);

        header(8, "Record a valid match result");
        succeed("Enter score for MAT-001: Thapar Hoopers 72 - Patiala Panthers 65", () -> {
            sys.matches().enterScore("MAT-001", 72, 65);
            return sys.getData().findMatch("MAT-001").toString();
        });
        System.out.print(indent(sys.reports().standings("TRN-001")));
        check("Aarav Mehta (PLR-001) has 1 win, Ishaan Kapoor (PLR-007) has 1 loss",
                sys.getData().findPlayer("PLR-001").getMatchesWon() == 1 && sys.getData().findPlayer("PLR-007").getMatchesLost() == 1);

        header(9, "Negative score");
        reject(InvalidScoreException.class, "Enter score for MAT-002: -4 - 50",
                () -> sys.matches().enterScore("MAT-002", -4, 50));
        check("MAT-002 is still SCHEDULED and no stats changed", sys.getData().findMatch("MAT-002").getStatus() == MatchStatus.SCHEDULED
                && sys.getData().findPlayer("PLR-001").getMatchesPlayed() == 1);

        header(10, "Fill a tournament, then register one more");
        succeed("Register PLR-023, PLR-024, PLR-025 in TRN-002 (maximum 4 entries)", () -> {
            sys.registrations().registerPlayerAndPay("TRN-002", "PLR-023", true);
            sys.registrations().registerPlayerAndPay("TRN-002", "PLR-024", true);
            sys.registrations().registerPlayerAndPay("TRN-002", "PLR-025", true);
            return "entries " + sys.getData().findTournament("TRN-002").activeEntryCount() + "/4";
        });
        reject(TournamentFullException.class, "Register Rishabh Saini (PLR-026) in TRN-002",
                () -> sys.registrations().registerPlayerAndPay("TRN-002", "PLR-026", true));
        check("TRN-002 still has 4 entries", sys.getData().findTournament("TRN-002").activeEntryCount() == 4);

        header(11, "Failed payment keeps data consistent (Freshers Cup TRN-003, fee multiplier 0.5)");
        reject(PaymentFailedException.class, "Register Simran Kaur (PLR-018) in TRN-003, gateway declines",
                () -> sys.registrations().registerPlayerAndPay("TRN-003", "PLR-018", false));
        check("rollback: TRN-003 has 0 entries", sys.getData().findTournament("TRN-003").activeEntryCount() == 0);
        Registration[] pending = new Registration[1];
        succeed("Reserve a slot for PLR-018 without paying", () -> {
            pending[0] = sys.registrations().registerPlayer("TRN-003", "PLR-018");
            return pending[0].getRegistrationId() + " is " + pending[0].getStatus();
        });
        reject(PaymentFailedException.class, "Pay the pending registration, gateway declines",
                () -> sys.payments().payPending(pending[0].getRegistrationId(), false));
        check("registration still PENDING and payment FAILED", pending[0].getStatus() == RegistrationStatus.PENDING
                && pending[0].getPayment().getStatus() == PaymentStatus.FAILED);
        succeed("Retry the payment, gateway approves", () -> {
            sys.payments().payPending(pending[0].getRegistrationId(), true);
            return sys.payments().receipt(pending[0]).replace("\n", " | ");
        });
        check("registration CONFIRMED", pending[0].getStatus() == RegistrationStatus.CONFIRMED);
        reject(InvalidOperationException.class, "Pay the same registration again (duplicate payment)",
                () -> sys.payments().payPending(pending[0].getRegistrationId(), true));

        header(12, "Save, restart, load");
        Path dir = Files.createTempDirectory("sportssphere-demo");
        FileDataStore store = new FileDataStore(dir.resolve("sportssphere.dat"));
        int players = sys.getData().getPlayers().size();
        int matches = sys.getData().getMatches().size();
        String standingsBefore = sys.reports().standings("TRN-001");
        succeed("Save all data to " + store.getPath().getFileName(), () -> {
            store.save(sys.getData());
            return "saved, file exists = " + store.exists();
        });
        SportsManagementSystem restarted = new SportsManagementSystem(store.load());   // fresh system, like a restart
        succeed("Start a new system and load the file", () ->
                "loaded " + restarted.getData().getPlayers().size() + " players, " + restarted.getData().getTeams().size()
                        + " teams, " + restarted.getData().getTournaments().size() + " tournaments, "
                        + restarted.getData().getMatches().size() + " matches");
        check("player and match counts equal", restarted.getData().getPlayers().size() == players
                && restarted.getData().getMatches().size() == matches);
        check("standings identical after reload", restarted.reports().standings("TRN-001").equals(standingsBefore));
        check("TRN-003 registration survived with CONFIRMED status",
                restarted.getData().findRegistration(pending[0].getRegistrationId()).getStatus() == RegistrationStatus.CONFIRMED);

        System.out.println();
        System.out.println("---------------- Extra cases for the remaining exceptions ----------------");
        header(13, "Invalid booking interval");
        reject(InvalidBookingException.class, "Book FAC-002 from 16:00 to 15:00 (end before start)", () ->
                sys.bookings().book("FAC-002", today.plusDays(2).atTime(16, 0), today.plusDays(2).atTime(15, 0), "x", "y"));
        reject(InvalidBookingException.class, "Book FAC-002 for 10 minutes (minimum 30)", () ->
                sys.bookings().book("FAC-002", today.plusDays(2).atTime(16, 0), today.plusDays(2).atTime(16, 10), "x", "y"));

        header(14, "Return equipment that was never issued");
        reject(EquipmentNotIssuedException.class, "Thapar Hoopers (TEM-001) return 1 basketball (EQP-001) that was never issued",
                () -> sys.equipment().returnItem("EQP-001", "TEM-001", 1, EquipmentCondition.GOOD));

        header(15, "Unknown player");
        reject(PlayerNotFoundException.class, "Register PLR-999 in TRN-002", () -> sys.registrations().registerPlayer("TRN-002", "PLR-999"));

        header(16, "Operation not allowed in current state");
        reject(InvalidOperationException.class, "Enter a second score for the completed MAT-001 (needs the correction process)",
                () -> sys.matches().enterScore("MAT-001", 10, 9));
        succeed("Correct MAT-001 properly: 72-65 becomes 64-65 with a reason", () -> {
            sys.matches().correctScore("MAT-001", 64, 65, "scorer error");
            return sys.getData().findMatch("MAT-001") + " | stats rebuilt: PLR-001 wins = " + sys.getData().findPlayer("PLR-001").getMatchesWon();
        });
        reject(InvalidOperationException.class, "Register a team in TRN-001 after the tournament has started",
                () -> sys.registrations().registerTeam("TRN-001", "TEM-003"));

        header(17, "Corrupted save file");
        Path bad = dir.resolve("corrupted.dat");
        Files.writeString(bad, "this is not a valid save file");
        reject(DataPersistenceException.class, "Load a corrupted file", () -> new FileDataStore(bad).load());

        System.out.println();
        System.out.print(sys.reports().failedOperations());
        System.out.println();
        System.out.println("RESULT: " + passed + " checks passed, " + failed + " failed.");
        return failed == 0;
    }

    private Equipment stock(String id) throws SportsException { return sys.getData().getInventory().find(id); }

    private void header(int n, String title) {
        System.out.println();
        System.out.println("=== Scenario " + n + ": " + title + " ===");
    }

    private void succeed(String input, Step step) {
        System.out.println("Input  : " + input);
        try {
            System.out.println("Result : [SUCCESS] " + step.run());
            passed++;
        } catch (Exception e) {
            System.out.println("Result : [UNEXPECTED FAILURE] " + e);
            failed++;
        }
    }

    private void reject(Class<? extends SportsException> expected, String input, Action action) {
        System.out.println("Input  : " + input);
        try {
            action.run();
            System.out.println("Result : [UNEXPECTED] no exception, expected " + expected.getSimpleName());
            failed++;
        } catch (SportsException e) {
            sys.logFailure(input, e);
            boolean right = expected.isInstance(e);
            System.out.println("Result : [REJECTED] " + e.getClass().getSimpleName() + " - " + e.getMessage());
            System.out.println("         expected " + expected.getSimpleName() + (right ? " -> PASS" : " -> FAIL"));
            if (right) passed++; else failed++;
        } catch (Exception e) {
            System.out.println("Result : [UNEXPECTED] " + e);
            failed++;
        }
    }

    private void check(String description, boolean condition) {
        System.out.println("Check  : " + description + " -> " + (condition ? "PASS" : "FAIL"));
        if (condition) passed++; else failed++;
    }

    private static String indent(String text) {
        StringBuilder sb = new StringBuilder();
        for (String line : text.split("\n")) sb.append("         ").append(line).append('\n');
        return sb.toString();
    }
}
