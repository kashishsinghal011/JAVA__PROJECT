package com.thapar.sportssphere.app;

import com.thapar.sportssphere.exception.InvalidOperationException;
import com.thapar.sportssphere.exception.SportsException;
import com.thapar.sportssphere.model.enums.EquipmentCondition;
import com.thapar.sportssphere.model.enums.SportType;
import com.thapar.sportssphere.service.PlayerTeamService;
import com.thapar.sportssphere.service.SportsManagementSystem;
import com.thapar.sportssphere.util.IdGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;

/**
 * Fictional demo data (invented names and roll numbers; nothing here comes from any real
 * Thapar system). Ids are deterministic because the counters are reset first, so the demo and
 * the documentation can refer to PLR-017, TEM-003, TRN-002 and so on.
 *
 * Players   PLR-001..006 Thapar Hoopers, 007..012 Patiala Panthers (basketball)
 *           PLR-013..015 Smash Squad, 019..020 Net Ninjas (badminton)
 *           PLR-016, 017, 018, 023..026 free badminton players
 *           PLR-021 is 16 years old (too young), PLR-022 is not registered with the sports office
 * Teams     TEM-001 Thapar Hoopers, TEM-002 Patiala Panthers, TEM-003 Smash Squad, TEM-004 Net Ninjas
 * Tournaments TRN-001 basketball league (IN_PROGRESS, 2 matches), TRN-002 Badminton Open (open, 4 entries max),
 *             TRN-003 Freshers Badminton Cup (open, half fee)
 * Facilities FAC-001/002 basketball courts, FAC-003 badminton hall, FAC-004 football ground
 * Equipment  EQP-001 basketballs, EQP-002 shuttlecock tubes, EQP-003 rackets, EQP-004 footballs
 */
public final class SampleDataLoader {
    private SampleDataLoader() { }

    private static final String[] HOOPERS = {"Aarav Mehta", "Vivaan Sharma", "Aditya Verma", "Arjun Singh", "Kabir Malhotra", "Rohan Gupta"};
    private static final String[] PANTHERS = {"Ishaan Kapoor", "Dev Chopra", "Krishna Bansal", "Yash Arora", "Harsh Jain", "Nikhil Garg"};
    private static final String[] BADMINTON = {"Ananya Rao", "Diya Khanna", "Meera Sethi", "Saanvi Bedi", "Tanya Walia",
            "Simran Kaur", "Karan Dhillon", "Pooja Anand", "Rahul Yadav", "Neha Tiwari", "Manav Soni", "Palak Goyal",
            "Jasleen Kaur", "Rishabh Saini"};

    /** Loads into an empty system only, so it can never overwrite real data. */
    public static void load(SportsManagementSystem sys) throws SportsException {
        if (!sys.getData().getPlayers().isEmpty() || !sys.getData().getTeams().isEmpty()) {
            throw new InvalidOperationException("sample data can only be loaded into an empty system");
        }
        IdGenerator.reset();
        PlayerTeamService people = sys.people();
        LocalDate today = LocalDate.now(sys.getClock());

        int roll = 100;
        for (int i = 0; i < HOOPERS.length; i++) {
            people.addPlayer(HOOPERS[i], 19 + i % 4, contact(HOOPERS[i]), "SMP" + (++roll), EnumSet.of(SportType.BASKETBALL), true);
        }
        for (int i = 0; i < PANTHERS.length; i++) {
            people.addPlayer(PANTHERS[i], 19 + i % 4, contact(PANTHERS[i]), "SMP" + (++roll), EnumSet.of(SportType.BASKETBALL), true);
        }
        for (int i = 0; i < BADMINTON.length; i++) {
            String name = BADMINTON[i];
            int age = name.equals("Rahul Yadav") ? 16 : 19 + i % 4;           // PLR-021: too young
            boolean registered = !name.equals("Neha Tiwari");                  // PLR-022: not registered
            people.addPlayer(name, age, contact(name), "SMP" + (++roll), EnumSet.of(SportType.BADMINTON), registered);
        }

        people.addAdministrator("Rajesh Bhatia", 45, "admin.sample@example.com", "SA-101");
        people.addCoach("Harpreet Singh", 38, "harpreet.sample@example.com", SportType.BASKETBALL);
        people.addCoach("Gurpreet Kaur", 35, "gurpreet.sample@example.com", SportType.BADMINTON);

        people.createTeam("Thapar Hoopers", SportType.BASKETBALL);   // TEM-001
        people.createTeam("Patiala Panthers", SportType.BASKETBALL); // TEM-002
        people.createTeam("Smash Squad", SportType.BADMINTON);       // TEM-003
        people.createTeam("Net Ninjas", SportType.BADMINTON);        // TEM-004
        for (int i = 1; i <= 6; i++) people.addPlayerToTeam("TEM-001", id(i));
        for (int i = 7; i <= 12; i++) people.addPlayerToTeam("TEM-002", id(i));
        for (int i = 13; i <= 15; i++) people.addPlayerToTeam("TEM-003", id(i));
        people.addPlayerToTeam("TEM-004", id(19));
        people.addPlayerToTeam("TEM-004", id(20));
        people.setCaptain("TEM-001", id(1));
        people.setCaptain("TEM-002", id(7));
        people.setCaptain("TEM-003", id(13));
        people.setCaptain("TEM-004", id(19));
        people.assignCoach("CCH-001", "TEM-001");
        people.assignCoach("CCH-001", "TEM-002");
        people.assignCoach("CCH-002", "TEM-003");
        people.assignCoach("CCH-002", "TEM-004");

        sys.bookings().addFacility("Basketball Court A", SportType.BASKETBALL);   // FAC-001
        sys.bookings().addFacility("Basketball Court B", SportType.BASKETBALL);   // FAC-002
        sys.bookings().addFacility("Indoor Badminton Hall", SportType.BADMINTON); // FAC-003
        sys.bookings().addFacility("Main Football Ground", SportType.FOOTBALL);   // FAC-004

        sys.equipment().addEquipment("Basketball (size 7)", 12, EquipmentCondition.NEW);  // EQP-001
        sys.equipment().addEquipment("Shuttlecock tube", 20, EquipmentCondition.GOOD);    // EQP-002
        sys.equipment().addEquipment("Badminton racket", 10, EquipmentCondition.GOOD);    // EQP-003
        sys.equipment().addEquipment("Football (size 5)", 8, EquipmentCondition.NEW);     // EQP-004

        LocalDate tomorrow = today.plusDays(1);
        sys.bookings().book("FAC-001", tomorrow.atTime(18, 0), tomorrow.atTime(19, 0), "Hoopers practice", "CCH-001");   // BKG-001
        sys.bookings().book("FAC-003", tomorrow.atTime(17, 0), tomorrow.atTime(18, 0), "Smash Squad practice", "CCH-002"); // BKG-002

        // TRN-001: two teams registered and paid, league started, two fixtures scheduled.
        sys.registrations().createTournament("Inter-Branch Basketball League", SportType.BASKETBALL, today.plusDays(20), 4, 1.0);
        sys.registrations().openRegistration("TRN-001");
        sys.registrations().registerTeamAndPay("TRN-001", "TEM-001", true);   // REG-001
        sys.registrations().registerTeamAndPay("TRN-001", "TEM-002", true);   // REG-002
        sys.registrations().closeRegistration("TRN-001");
        sys.registrations().startTournament("TRN-001");
        sys.matches().schedule("TRN-001", "TEM-001", "TEM-002", "FAC-001", LocalDateTime.of(today.plusDays(3), LocalTime.of(16, 0))); // MAT-001
        sys.matches().schedule("TRN-001", "TEM-001", "TEM-002", "FAC-001", LocalDateTime.of(today.plusDays(5), LocalTime.of(16, 0))); // MAT-002

        // TRN-002 and TRN-003: open for individual badminton entries, nobody registered yet.
        sys.registrations().createTournament("Badminton Open", SportType.BADMINTON, today.plusDays(20), 4, 1.0);
        sys.registrations().openRegistration("TRN-002");
        sys.registrations().createTournament("Freshers Badminton Cup", SportType.BADMINTON, today.plusDays(25), 8, 0.5);
        sys.registrations().openRegistration("TRN-003");
    }

    private static String id(int n) { return String.format("PLR-%03d", n); }

    private static String contact(String name) {
        return name.toLowerCase().replace(' ', '.') + ".sample@example.com";
    }
}
