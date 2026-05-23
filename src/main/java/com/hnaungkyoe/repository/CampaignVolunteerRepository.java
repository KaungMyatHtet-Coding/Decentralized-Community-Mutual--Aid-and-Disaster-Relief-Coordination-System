package com.hnaungkyoe.repository;

import com.hnaungkyoe.entity.CampaignVolunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignVolunteerRepository extends JpaRepository<CampaignVolunteer, Long> {
}