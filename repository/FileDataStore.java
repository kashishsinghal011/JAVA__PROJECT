package com.thapar.sportssphere.repository;

import com.thapar.sportssphere.exception.DataPersistenceException;
import com.thapar.sportssphere.model.SportsData;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Java-serialization store.
 *
 * Safe save: data is written to "<file>.tmp" first and only then moved over the real file, so a
 * failure half-way (disk full, crash) leaves the previous save untouched. The previous save is
 * also copied to "<file>.bak" before it is replaced.
 *
 * Safe load: missing, corrupted, incompatible or foreign files each give a clear message and
 * never crash the program. An ObjectInputFilter only lets this project's classes and basic
 * JDK types be deserialized, so a tampered file cannot instantiate arbitrary classes.
 */
public class FileDataStore implements DataStore {
    private static final String ALLOWED_CLASSES =
            "com.thapar.sportssphere.**;java.lang.*;java.util.*;java.time.*;!*";

    private final Path file;

    public FileDataStore(Path file) { this.file = file; }

    public Path getPath() { return file; }

    @Override
    public boolean exists() { return Files.isRegularFile(file); }

    @Override
    public void save(SportsData data) throws DataPersistenceException {
        Path tmp = sibling(".tmp");
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(tmp)))) {
                out.writeObject(data);
            }
            if (exists()) {
                Files.copy(file, sibling(".bak"), StandardCopyOption.REPLACE_EXISTING);
            }
            replace(tmp);
        } catch (IOException e) {
            removeQuietly(tmp);       // do not leave a half-written temp file behind
            throw new DataPersistenceException("Could not save to " + file + " (" + e.getMessage()
                    + "). Your previous save, if any, was not changed.", e);
        }
    }

    @Override
    public SportsData load() throws DataPersistenceException {
        if (!exists()) {
            throw new DataPersistenceException("No saved data found at " + file.toAbsolutePath()
                    + ". Save first, or start with an empty system.");
        }
        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(file)))) {
            in.setObjectInputFilter(ObjectInputFilter.Config.createFilter(ALLOWED_CLASSES));
            Object loaded = in.readObject();
            if (!(loaded instanceof SportsData data)) {
                throw new DataPersistenceException(file + " does not contain SportsSphere data.");
            }
            data.observeIds();        // keep newly generated ids from colliding with loaded ones
            return data;
        } catch (InvalidClassException e) {
            throw new DataPersistenceException("The save file was written by an incompatible version of the program ("
                    + e.getMessage() + "). Reset the data file to continue.", e);
        } catch (StreamCorruptedException | EOFException e) {
            throw new DataPersistenceException("The save file " + file + " is corrupted or incomplete. Delete it"
                    + " (a backup of the previous save may exist as " + sibling(".bak").getFileName() + ").", e);
        } catch (ClassNotFoundException e) {
            throw new DataPersistenceException("The save file refers to a class this program does not have: "
                    + e.getMessage(), e);
        } catch (IOException e) {
            throw new DataPersistenceException("Could not read " + file + ": " + e.getMessage(), e);
        }
    }

    /** Atomic replace where the file system supports it, plain replace otherwise. */
    private void replace(Path tmp) throws IOException {
        try {
            Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Path sibling(String suffix) { return file.resolveSibling(file.getFileName() + suffix); }

    private void removeQuietly(Path p) {
        try {
            Files.deleteIfExists(p);
        } catch (IOException ignored) {
            // Nothing more can be done; the original error is what the caller needs to see.
        }
    }
}
