package com.thapar.sportssphere.model;

import com.thapar.sportssphere.model.contract.Payable;
import com.thapar.sportssphere.util.Validator;

import java.io.Serializable;

/**
 * ABSTRACTION + INHERITANCE: common identity data for everyone in the system.
 * Never instantiated directly; subclasses supply displayDetails() and calculateFee().
 * ENCAPSULATION: all fields private, validation runs in the constructor and setters.
 */
public abstract class Person implements Serializable, Payable {
    private static final long serialVersionUID = 1L;

    public static final int MIN_AGE = 10;
    public static final int MAX_AGE = 100;

    private final String id;
    private String name;
    private int age;
    private String contact;

    protected Person(String id, String name, int age, String contact) {
        this.id = Validator.requireNonBlank(id, "ID");
        this.name = Validator.requireNonBlank(name, "Name");
        this.age = Validator.requireRange(age, MIN_AGE, MAX_AGE, "Age");
        this.contact = (contact == null || contact.isBlank()) ? "N/A" : contact.trim();
    }

    /** Constructor overloading: contact is optional. */
    protected Person(String id, String name, int age) {
        this(id, name, age, "N/A");
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getContact() { return contact; }

    public void setName(String name) { this.name = Validator.requireNonBlank(name, "Name"); }

    public void setAge(int age) { this.age = Validator.requireRange(age, MIN_AGE, MAX_AGE, "Age"); }

    public void setContact(String contact) {
        this.contact = (contact == null || contact.isBlank()) ? "N/A" : contact.trim();
    }

    /** Polymorphic: each subclass prints its own profile. */
    public abstract void displayDetails();

    /** Shared first line of every profile, reused by subclasses through super. */
    protected String baseDetails() {
        return String.format("%s | %s | Age %d | Contact %s", id, name, age, contact);
    }

    @Override
    public String toString() { return id + " - " + name; }
}
