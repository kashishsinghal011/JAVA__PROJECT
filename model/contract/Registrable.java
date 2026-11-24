package com.thapar.sportssphere.model.contract;

/** Anything that must be registered with the sports office before it can compete. */
public interface Registrable {
    boolean isRegistered();
    void markRegistered();
    void revokeRegistration();
}
