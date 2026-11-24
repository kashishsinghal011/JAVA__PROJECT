package com.thapar.sportssphere.web.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CoachRepository extends JpaRepository<CoachEntity, String> {
}
