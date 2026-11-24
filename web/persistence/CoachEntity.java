package com.thapar.sportssphere.web.persistence;

import com.thapar.sportssphere.model.enums.SportType;
import jakarta.persistence.*;

/** Coach row. */
@Entity
@Table(name = "coaches")
public class CoachEntity {

    @Id
    @Column(name = "coach_id", length = 32)
    private String coachId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false, length = 100)
    private String contact;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SportType specialization;

    public CoachEntity() {
    }

    public String getCoachId() {
        return coachId;
    }

    public void setCoachId(String coachId) {
        this.coachId = coachId;
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

    public SportType getSpecialization() {
        return specialization;
    }

    public void setSpecialization(SportType specialization) {
        this.specialization = specialization;
    }
}
