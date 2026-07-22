package com.hnaungkyoe.repository;

import com.hnaungkyoe.entity.AidRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AidRequestRepository extends JpaRepository<AidRequest, Long> {
    // ✅ categories အတွက် အသစ်
    List<AidRequest> findByCategoriesContaining(AidRequest.Category category);
    List<AidRequest> findByStatus(AidRequest.Status status);
    long countByStatus(AidRequest.Status status);
    long countByStatusAndTownship(AidRequest.Status status, String township);
    List<AidRequest> findByReporterId(Long reporterId);
    List<AidRequest> findByTownshipAndStatus(String township, AidRequest.Status status);
    List<AidRequest> findByTownship(String township);
    List<AidRequest> findByAssignedVolunteerId(Long volunteerId);
    // Find requests that contain ANY of the given categories
    @Query("SELECT DISTINCT a FROM AidRequest a JOIN a.categories c WHERE c IN :categories")
    List<AidRequest> findByCategoriesIn(@Param("categories") List<AidRequest.Category> categories);
}