package com.hnaungkyoe.repository;

import com.hnaungkyoe.entity.EmergencyUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyUsageLogRepository extends JpaRepository<EmergencyUsageLog, Long> {
    List<EmergencyUsageLog> findByTownshipOrderByUsageDateDesc(String township);
    List<EmergencyUsageLog> findAllByOrderByUsageDateDesc();
}
