package com.hnaungkyoe.repository;

import com.hnaungkyoe.entity.VolunteerApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VolunteerApplicationRepository extends JpaRepository<VolunteerApplication, Long> {
    List<VolunteerApplication> findByStatus(VolunteerApplication.Status status);
    boolean existsByUserId(Long userId);
    Optional<VolunteerApplication> findByUserId(Long userId);
}