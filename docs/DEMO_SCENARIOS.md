# Demo scenarios (sample input, expected output, exception, explanation)

Run all of them automatically: `./demo.sh` (Windows: `demo.bat`), or
`java -cp out com.thapar.sportssphere.app.DemoRunner`. The captured output of one run is in
`docs/DEMO_OUTPUT.txt`. Dates in messages are relative to the day you run it.

To try them by hand, start with `./run.sh --sample` and use the **Menu path** column. Ids come from the
fictional sample data (see README). Payment questions are the simulated gateway: `y` approves, `n` declines.

| # | Scenario | Menu path (inputs after the option) | Expected output | Exception | Why it happens |
|---|---|---|---|---|---|
| 1 | Register a player successfully | `5` > `1` > `TRN-002`, `PLR-017`, `y` | `Registered and paid.` receipt: Rs 200.00, registration CONFIRMED | none | Player is registered with the sports office, age 17-28, prefers badminton; slot free; simulated payment approved |
| 2 | Same registration twice | `5` > `1` > `TRN-002`, `PLR-017`, `y` | `[REJECTED] Tanya Walia is already registered for Badminton Open.` | `DuplicateRegistrationException` | `Tournament.addRegistration` finds the player id among active entries; entry count stays 1 |
| 3 | Team capacity | `3` > `2` > `TEM-003`, `PLR-016` (OK, 4/4); then `3` > `2` > `TEM-003`, `PLR-018` | `[REJECTED] Team Smash Squad is full (maximum 4 players).` | `TeamCapacityExceededException` | `Team.addPlayer` compares roster size with `Sport.getMaxTeamSize()` (badminton: 4); roster stays at 4 |
| 4 | Overlapping facility booking | `6` > `3` > `FAC-001`, date, `10:00`, `11:00`, purpose, name; then same date `10:30`-`11:30` | second attempt: `[REJECTED] Basketball Court A is not available; it overlaps with BKG-... (Fitness session ...)` | `FacilityUnavailableException` | `SportsFacility.book` finds a confirmed booking where `start < other.end && other.start < end`; a back-to-back 11:00-12:00 booking is accepted |
| 5 | Insufficient equipment | `7` > `2` > `EQP-002`, `TEM-003`, `12` (OK); then `7` > `2` > `EQP-002`, `TEM-004`, `10` | `[REJECTED] Cannot issue 10 x Shuttlecock tube; only 8 available.` | `InsufficientEquipmentException` | `Equipment.issue` checks quantity against available stock before changing it; stock stays 8 |
| 6 | Return equipment | `7` > `3` > `EQP-002`, `TEM-003`, `12`, condition `3` (WORN) | `Returned 12 item(s); stock restored.` Availability report shows 20/20, condition WORN | none | `EquipmentInventory.returnItem` verifies the holder was issued 12, then restores stock and keeps the worst condition seen |
| 7 | Ineligible players | `5` > `1` > `TRN-002`, `PLR-021`, `y`; then `PLR-022` | `[REJECTED] Rahul Yadav is not eligible: age 16 is outside the 17-28 band for Badminton`; `[REJECTED] Neha Tiwari is not eligible: not registered with the sports office` | `IneligiblePlayerException` | `Sport.checkEligibility` checks age band, sports-office registration and sport preference |
| 8 | Valid match result | `9` > `1` > `MAT-001`, `72`, `65` | `Result recorded.`; standings (option `10` > `1` > `TRN-001`) show Thapar Hoopers 3 pts, 100% | none | `Match.recordScore` accepts the score; `AnalyticsService.applyResult` credits each roster |
| 9 | Negative score | `9` > `1` > `MAT-002`, `-4`, `50` | `[REJECTED] Invalid score: scores cannot be negative (got -4 - 50)` | `InvalidScoreException` | `Match.validateScore` rejects it before any state changes; match stays SCHEDULED and player stats are untouched |
| 10 | Fill a tournament, then one more | `5` > `1` > `TRN-002` with `PLR-023`, `PLR-024`, `PLR-025` (all `y`); then `PLR-026` | `[REJECTED] Tournament Badminton Open is full (limit 4 entries).` | `TournamentFullException` | `Tournament.isFull()` counts active entries (4/4, with scenario 1's entry) |
| 11 | Failed payment, consistent data | `5` > `1` > `TRN-003`, `PLR-018`, `n` | `[REJECTED] Payment PAY-REG-... failed (simulated): the simulated gateway declined the transaction`; TRN-003 still has 0 entries | `PaymentFailedException` | `RegistrationService.payOrRollback` removes the reserved slot. With reserve-then-pay (`5` > `3`, then `5` > `5` with `n`) the registration stays PENDING and the payment FAILED until a successful retry; paying twice gives `InvalidOperationException` |
| 12 | Save, restart, load | `12`; quit; start `./run.sh` again (it auto-loads) or use `13` | `Data saved.` ... `Loaded saved data from data/sportssphere.dat` | none | `FileDataStore` writes a temp file, then atomically replaces the real one; a `.bak` of the previous save is kept |

## Extra cases for the remaining exceptions

| # | Scenario | Expected output | Exception |
|---|---|---|---|
| 13 | Book 16:00-15:00, or 10 minutes | `Invalid booking: end time must be after start time` / `duration must be between 30 and 240 minutes (got 10)` | `InvalidBookingException` |
| 14 | Return a basketball nobody was issued | `Cannot return 1 x Basketball (size 7) for TEM-001; only 0 issued.` | `EquipmentNotIssuedException` |
| 15 | Register `PLR-999` | `No player found with id PLR-999.` | `PlayerNotFoundException` |
| 16 | Score a completed match again; register after the tournament started | `match MAT-001 already has a result; use the correction process`; `registration for Inter-Branch Basketball League is not open (status IN_PROGRESS)` | `InvalidOperationException` |
| 17 | Load a corrupted file | `The save file ... is corrupted or incomplete. Delete it ...` | `DataPersistenceException` |

Exception to scenario mapping: all 12 exceptions required by the project brief are triggered (1-12 and 13-16), plus
`DataPersistenceException` (17).
