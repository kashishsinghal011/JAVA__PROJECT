package com.thapar.sportssphere.model;

import com.thapar.sportssphere.util.Validator;

/** Staff member who runs registrations, facilities and tournaments. */
public class SportsAdministrator extends Person {
    private static final long serialVersionUID = 1L;

    private final String employeeId;

    public SportsAdministrator(String id, String name, int age, String contact, String employeeId) {
        super(id, name, age, contact);
        this.employeeId = Validator.requireNonBlank(employeeId, "Employee id");
    }

    public String getEmployeeId() { return employeeId; }

    @Override
    public double calculateFee() { return 0.0; }   // staff are not charged

    @Override
    public void displayDetails() {
        System.out.println("[ADMIN] " + baseDetails());
        System.out.println("   Employee id: " + employeeId);
    }
}
