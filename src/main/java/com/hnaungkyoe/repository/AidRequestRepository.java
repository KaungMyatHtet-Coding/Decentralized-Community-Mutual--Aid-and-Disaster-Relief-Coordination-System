package com.hnaungkyoe.repository;

import com.hnaungkyoe.entity.AidRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AidRequestRepository extends JpaRepository<AidRequest, Long> {
}