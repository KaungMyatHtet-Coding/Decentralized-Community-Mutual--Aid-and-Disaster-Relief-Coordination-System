package com.hnaungkyoe.repository;

import com.hnaungkyoe.entity.DeliveryReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryReportRepository extends JpaRepository<DeliveryReport, Long> {
    List<DeliveryReport> findByVolunteerId(Long volunteerId);
    List<DeliveryReport> findByStatus(DeliveryReport.Status status);
    Optional<DeliveryReport> findByAidRequestIdAndVolunteerId(Long aidRequestId, Long volunteerId);

    // Admin sees pending reports for their township's requests
    List<DeliveryReport> findByAidRequest_TownshipAndStatus(String township, DeliveryReport.Status status);
    List<DeliveryReport> findByAidRequest_Township(String township);
}
