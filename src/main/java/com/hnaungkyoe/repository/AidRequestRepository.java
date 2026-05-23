package com.hnaungkyoe.repository;

import com.hnaungkyoe.entity.AidRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AidRequestRepository extends JpaRepository<AidRequest, Long> {
    List<AidRequest> findByTownship(String township);
    List<AidRequest> findByStatus(AidRequest.Status status);
    List<AidRequest> findByReporterId(Long reporterId);
    List<AidRequest> findByTownshipAndStatus(String township, AidRequest.Status status);
}