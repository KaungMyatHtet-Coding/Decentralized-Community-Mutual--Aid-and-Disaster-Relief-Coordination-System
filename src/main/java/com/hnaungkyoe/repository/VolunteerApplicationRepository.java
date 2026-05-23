package com.hnaungkyoe.repository;

import com.hnaungkyoe.entity.VolunteerApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VolunteerApplicationRepository extends JpaRepository<VolunteerApplication, Long> {
}