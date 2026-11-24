package com.thapar.sportssphere.web.persistence;

import jakarta.persistence.*;

/** Administrator row. */
@Entity
@Table(name = "administrators")
public class AdministratorEntity {

    @Id
    @Column(name = "admin_id", length = 32)
    private String adminId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false, length = 100)
    private String contact;

    @Column(name = "employee_id", nullable = false, length = 40)
    private String employeeId;

    public AdministratorEntity() {
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
}
