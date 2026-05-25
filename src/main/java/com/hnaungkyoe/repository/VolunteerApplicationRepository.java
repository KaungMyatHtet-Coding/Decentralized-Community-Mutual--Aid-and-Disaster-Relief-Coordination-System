package com.hnaungkyoe.repository;

import com.hnaungkyoe.entity.VolunteerApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VolunteerApplicationRepository extends JpaRepository<VolunteerApplication, Long> {

    // ✅ Status enum (not String)
    List<VolunteerApplication> findByStatus(VolunteerApplication.Status status);

    // ✅ unchanged - still valid
    boolean existsByUserId(Long userId);

    // ✅ unchanged - still valid
    Optional<VolunteerApplication> findByUserId(Long userId);

    // ✅ Status enum, correct field name appliedAt (not createdAt)
    long countByStatus(VolunteerApplication.Status status);

    // ✅ Status enum, correct field appliedAt, correct relation user_ (not applicant_)
    List<VolunteerApplication> findByStatusOrderByAppliedAtDesc(VolunteerApplication.Status status);

    // ✅ Status enum, correct field appliedAt, correct relation user_ (not applicant_)
    List<VolunteerApplication> findByStatusAndUser_UsernameContainingIgnoreCaseOrderByAppliedAtDesc(
            VolunteerApplication.Status status, String username);
    // ဒါ ထည့်ပေး — township နဲ့ volunteer ရှာဖို့
    List<VolunteerApplication> findByStatusAndOperatingTownship(
            VolunteerApplication.Status status,
            String operatingTownship
    );
}