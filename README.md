# Thapar SportsSphere: Campus Sports and Tournament Management System

A console-based Java 17 application that simulates sports operations on a campus: players, coaches,
teams, tournaments, facilities, equipment, match scheduling, results, analytics and reports. It is an
educational OOP project with a real custom-exception hierarchy.

> This is a student project. It is **not** affiliated with, or connected to, any official Thapar Institute
> system. All sample data (names, roll numbers, e-mails) is fictional, and payments are simulated: no card,
> UPI or bank data is ever collected. There is no ID-card feature.

## Requirements

- JDK 17 or newer (`javac -version`). No external libraries are needed to run the application.
- JUnit 5 is needed only to run the unit tests (Maven downloads it).

## Quick start

```
./run.sh --sample        # Windows: run.bat --sample   (menu with fictional sample data)
./demo.sh                # Windows: demo.bat           (all demo scenarios, no typing)
```

Or by hand:

```
mkdir out
javac -d out $(find src/main/java -name "*.java")      # Windows: use run.bat, or add the files to an @argfile
java -cp out com.thapar.sportssphere.app.Main --sample
java -cp out com.thapar.sportssphere.app.DemoRunner
```

| Command | Effect |
|---|---|
| `Main` | Start the menu; loads `data/sportssphere.dat` if it exists, otherwise starts empty |
| `Main --sample` | Start with fictional sample data (nothing is read from disk) |
| `Main --reset` | Delete the saved file, then start empty (`--reset --sample` gives fresh sample data) |
| `Main other.dat` | Use another data file |
| `DemoRunner` | Run the 12 required scenarios plus extra exception cases; exit code 0 means every check passed |

In the menu, type `cancel` at any prompt to abandon the current action. Choose 12 to save and 13 to load.

## Data file and reset

- Saved data: `data/sportssphere.dat` (relative to the folder you start the program from).
- Every save also keeps the previous version as `data/sportssphere.dat.bak`. A save is written to
  `sportssphere.dat.tmp` first and moved into place, so a failed save never destroys the old file.
- Reset: run with `--reset`, or delete the `data` folder.
- A missing, corrupted or incompatible file never crashes the program; you get a message and an empty system.

## Setup in VS Code

1. Install the "Extension Pack for Java" and open the `ThaparSportsSphere` folder.
2. Wait for the Java project to import (it detects `pom.xml`).
3. Open `app/Main.java` and click **Run**. To pass `--sample`, add `"args": ["--sample"]` to the launch
   configuration (Run and Debug > create a launch.json). The menu reads from the integrated terminal.

## Setup in IntelliJ IDEA

1. File > Open > choose the `ThaparSportsSphere` folder (it imports as a Maven project).
2. Set Project SDK to 17 or newer.
3. Open `Main.java`, click the green arrow > Modify Run Configuration > Program arguments: `--sample`.
4. Run. Tests: right-click `src/test/java` > Run All Tests (or `mvn test`).

## Project layout

```
src/main/java/com/thapar/sportssphere/
  app/         Main, ConsoleMenu, SampleDataLoader, DemoRunner
  model/       Person (abstract), Player, Coach, SportsAdministrator, Sport, Team, Tournament, Registration,
               Payment, Match, SportsFacility, FacilityBooking, TimeSlot, Equipment, EquipmentInventory, SportsData
    enums/     SportType, TournamentStatus, BookingStatus, EquipmentCondition, MatchStatus, RegistrationStatus,
               PaymentStatus, MatchOutcome
    contract/  Registrable, Payable, Reportable
  service/     SportsManagementSystem (facade), PlayerTeamService, RegistrationService, PaymentService,
               BookingService, EquipmentService, MatchService, AnalyticsService, TeamStanding, ReportService
  repository/  DataStore (interface), FileDataStore
  exception/   SportsException and 16 subclasses
  util/        Validator, IdGenerator, InputReader, DateTimeUtil, FailureLog, InputClosedException,
               OperationCancelledException
src/test/java/com/thapar/sportssphere/   JUnit 5 tests (model, service, repository)
docs/          DEMO_SCENARIOS.md, DEMO_OUTPUT.txt
```

Rule of thumb: the model protects its own invariants and throws; services coordinate and keep operations
all-or-nothing; the menu (`ConsoleMenu.execute`) is the only place that catches business exceptions, prints a
friendly message and records the failure; `Main` holds no business rules.

## Fictional sample data (`--sample`)

| Ids | What |
|---|---|
| PLR-001..006, PLR-007..012 | Basketball players, teams TEM-001 Thapar Hoopers and TEM-002 Patiala Panthers |
| PLR-013..015, PLR-019..020 | Badminton players in TEM-003 Smash Squad and TEM-004 Net Ninjas |
| PLR-016, 017, 018, 023..026 | Free badminton players (used by the demo registrations) |
| PLR-021 | Age 16, too young for any tournament (ineligible) |
| PLR-022 | Not registered with the sports office (ineligible) |
| CCH-001, CCH-002, ADM-001 | Basketball coach, badminton coach, administrator |
| TRN-001 | Inter-Branch Basketball League, IN_PROGRESS, both basketball teams confirmed |
| MAT-001, MAT-002 | Fixtures of TRN-001 (3 and 5 days from today), not yet scored |
| TRN-002 | Badminton Open, registration open, maximum 4 entries, normal fee |
| TRN-003 | Freshers Badminton Cup, registration open, half fee |
| FAC-001, 002 / 003 / 004 | Basketball courts A and B / indoor badminton hall / football ground |
| EQP-001..004 | Basketballs (12), shuttlecock tubes (20), rackets (10), footballs (8) |

Dates are relative to the day you start the program, so "tomorrow" bookings and upcoming matches are always valid.
See `docs/DEMO_SCENARIOS.md` for the menu path of every scenario.

## Tests

```
mvn test
```

50 tests cover team capacity and eligibility, booking overlap and free slots, equipment ledger rules, tournament
duplicate/full/deadline rules, scoring and correction, payment and registration consistency, atomic scheduling,
tie-break ranking, divide-by-zero guards and the file store (corrupted, truncated, failed save).

Note on verification: I could not download JUnit or Maven in the environment where this was written, so
`pom.xml` and `mvn test` are untested there. The tests were compiled and run with a tiny stand-in for the
annotations and assertions they use (`@Test`, `@BeforeEach`, `assertEquals`, `assertThrows`, ...), and all 50 passed.
The demo runner and all main code were compiled and run with a real JDK 21.

## Known limitations (good viva material)

- Player statistics are rebuilt from completed matches using **current** rosters, so changing a roster
  after matches are played changes what a rebuild credits.
- A tournament entry is a team or an individual player; the maximum counts both kinds together.
- Java serialization ties the save file to the class definitions: after changing the model classes, reset the data file.
- Single user, single process: nothing guards against two programs saving the same file.
- Input that the model rejects (for example age -5) can skip an id number; ids are unique but not always consecutive.
