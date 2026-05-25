package com.hnaungkyoe.repository;

import com.hnaungkyoe.entity.ItemDonation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemDonationRepository extends JpaRepository<ItemDonation, Long> {

    List<ItemDonation> findByDonorId(Long donorId);
    List<ItemDonation> findByCampaignId(Long campaignId);
    List<ItemDonation> findByStatus(ItemDonation.Status status);
    List<ItemDonation> findByAssignedVolunteerId(Long volunteerId);

    long countByStatus(ItemDonation.Status status);

    // Donor township နဲ့ volunteer township match လုပ်ပြီး assign
    @Query("""
        SELECT va FROM VolunteerApplication va
        WHERE va.operatingTownship = :township
        AND va.status = 'APPROVED'
    """)
    List<Object[]> findAvailableVolunteersByTownship(
            @Param("township") String township
    );

    // Admin — status အလိုက် စစ်
    List<ItemDonation> findByStatusOrderByCreatedAtDesc(ItemDonation.Status status);

    // Volunteer ရဲ့ assigned items
    List<ItemDonation> findByAssignedVolunteerIdAndStatus(
            Long volunteerId, ItemDonation.Status status
    );
}