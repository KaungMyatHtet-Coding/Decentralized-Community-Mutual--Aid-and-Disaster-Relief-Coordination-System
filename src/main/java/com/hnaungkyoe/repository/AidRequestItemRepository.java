package com.hnaungkyoe.repository;

import com.hnaungkyoe.entity.AidRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AidRequestItemRepository extends JpaRepository<AidRequestItem, Long> {
    List<AidRequestItem> findByAidRequestId(Long aidRequestId);
    void deleteByAidRequestId(Long aidRequestId);
}
