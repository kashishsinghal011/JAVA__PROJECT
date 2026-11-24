package com.thapar.sportssphere.repository;

import com.thapar.sportssphere.exception.DataPersistenceException;
import com.thapar.sportssphere.model.SportsData;

/**
 * ABSTRACTION: the services and menu depend on this interface, not on files.
 * A CSV or database store could replace FileDataStore without touching business code.
 */
public interface DataStore {
    void save(SportsData data) throws DataPersistenceException;

    SportsData load() throws DataPersistenceException;

    boolean exists();
}
