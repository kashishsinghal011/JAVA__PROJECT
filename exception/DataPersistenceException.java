package com.thapar.sportssphere.exception;

/** Saving or loading data failed (missing, corrupted or incompatible file, I/O error). */
public class DataPersistenceException extends SportsException {
    private static final long serialVersionUID = 1L;

    public DataPersistenceException(String message) { super(message); }

    public DataPersistenceException(String message, Throwable cause) { super(message, cause); }
}
