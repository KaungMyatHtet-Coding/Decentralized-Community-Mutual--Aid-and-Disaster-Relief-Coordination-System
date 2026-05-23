package com.hnaungkyoe.repository;

import com.hnaungkyoe.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByCampaignId(Long campaignId);
    List<Donation> findByDonorId(Long donorId);
    List<Donation> findByStatus(Donation.Status status);

    @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.campaign.id = :campaignId AND d.status = 'CONFIRMED'")
    BigDecimal sumConfirmedAmountByCampaignId(@Param("campaignId") Long campaignId);
}