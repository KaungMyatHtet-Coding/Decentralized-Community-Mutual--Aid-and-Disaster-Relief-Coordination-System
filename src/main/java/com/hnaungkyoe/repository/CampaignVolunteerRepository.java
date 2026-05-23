package com.hnaungkyoe.repository;

import com.hnaungkyoe.entity.CampaignVolunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CampaignVolunteerRepository extends JpaRepository<CampaignVolunteer, Long> {
    List<CampaignVolunteer> findByCampaignId(Long campaignId);
    List<CampaignVolunteer> findByUserId(Long userId);
    boolean existsByCampaignIdAndUserId(Long campaignId, Long userId);
}