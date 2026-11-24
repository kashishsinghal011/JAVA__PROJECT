package com.thapar.sportssphere.app;

import com.thapar.sportssphere.exception.DataPersistenceException;
import com.thapar.sportssphere.exception.SportsException;
import com.thapar.sportssphere.repository.FileDataStore;
import com.thapar.sportssphere.service.SportsManagementSystem;
import com.thapar.sportssphere.util.InputReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Entry point only: choose the data file, load it if it exists, start the menu.
 * No business rules live here.
 *
 *   java -cp out com.thapar.sportssphere.app.Main            use data/sportssphere.dat
 *   java -cp out com.thapar.sportssphere.app.Main --reset    delete the saved file, start empty
 *   java -cp out com.thapar.sportssphere.app.Main --sample  start with fictional demo data (nothing is loaded from disk)
 *   java -cp out com.thapar.sportssphere.app.Main myfile.dat use another file
 */
public class Main {
    public static final String DEFAULT_FILE = "data/sportssphere.dat";

    public static void main(String[] args) {
        boolean reset = false;
        boolean sample = false;
        Path file = Paths.get(DEFAULT_FILE);
        for (String arg : args) {
            if (arg.equals("--reset")) {
                reset = true;
            } else if (arg.equals("--sample")) {
                sample = true;
            } else if (!arg.startsWith("--")) {
                file = Paths.get(arg);
            }
        }
        FileDataStore store = new FileDataStore(file);

        if (reset) {
            try {
                boolean deleted = Files.deleteIfExists(file);
                System.out.println(deleted ? "Saved data deleted: " + file : "No saved data to delete at " + file);
            } catch (IOException e) {
                System.out.println("Could not delete " + file + ": " + e.getMessage());
            }
        }

        SportsManagementSystem system = new SportsManagementSystem();
        if (sample) {
            try {
                SampleDataLoader.load(system);
                System.out.println("Fictional sample data loaded (not read from disk). Saving (option 12) would overwrite " + file);
            } catch (SportsException e) {
                System.out.println("[WARNING] Could not load sample data: " + e.getMessage());
            }
        } else if (store.exists()) {
            try {
                system = new SportsManagementSystem(store.load());
                System.out.println("Loaded saved data from " + file);
            } catch (DataPersistenceException e) {
                System.out.println("[WARNING] " + e.getMessage());
                System.out.println("Starting with an empty system; the file was left untouched.");
            }
        } else {
            System.out.println("No saved data found (" + file + "); starting with an empty system.");
        }

        InputReader in = new InputReader(System.in, System.out);
        new ConsoleMenu(system, in, store, System.out).run();
    }
}
