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
    long countByStatusAndDonorTownship(ItemDonation.Status status, String donorTownship);
    List<ItemDonation> findByStatusOrderByCreatedAtDesc(ItemDonation.Status status);
    List<ItemDonation> findByAssignedVolunteerIdAndStatus(Long volunteerId, ItemDonation.Status status);

    // ─── 💡 Volunteer active assignments (Enum တန်ဖိုး တိုက်ရိုက်ပြင်ဆင်ပြီး) ─
    @Query("""
        SELECT d FROM ItemDonation d
        WHERE d.assignedVolunteer.id = :volunteerId
        AND d.status IN (com.hnaungkyoe.entity.ItemDonation.Status.ASSIGNED_TO_VOLUNTEER, com.hnaungkyoe.entity.ItemDonation.Status.VOLUNTEER_ACCEPTED)
        ORDER BY d.updatedAt DESC
    """)
    List<ItemDonation> findActiveAssignments(@Param("volunteerId") Long volunteerId);

    // ─── 💡 Volunteer history (မလိုလားအပ်တဲ့ status တွေကို ရှင်းလင်းပြီး) ───
    @Query("""
        SELECT d FROM ItemDonation d
        WHERE d.assignedVolunteer.id = :volunteerId
        AND d.status IN (com.hnaungkyoe.entity.ItemDonation.Status.VOLUNTEER_RECEIVED, com.hnaungkyoe.entity.ItemDonation.Status.STORED_IN_STOCK, com.hnaungkyoe.entity.ItemDonation.Status.VOLUNTEER_REJECTED, com.hnaungkyoe.entity.ItemDonation.Status.COMPLETED)
        ORDER BY d.updatedAt DESC
    """)
    List<ItemDonation> findVolunteerHistory(@Param("volunteerId") Long volunteerId);

    // ─── 💡 Admin pending review queue (PENDING_ADMIN သို့ ပြောင်းလဲပြီး) ─────────────────────────
    @Query("""
        SELECT d FROM ItemDonation d
        WHERE d.status = com.hnaungkyoe.entity.ItemDonation.Status.PENDING_ADMIN
        ORDER BY d.createdAt ASC
    """)
    List<ItemDonation> findPendingAdminReview();

    // ─── Multiple status filter (admin all view) ─────────────
    @Query("""
        SELECT d FROM ItemDonation d
        WHERE d.status IN :statuses
        ORDER BY d.createdAt DESC
    """)
    List<ItemDonation> findByStatusIn(@Param("statuses") List<ItemDonation.Status> statuses);

    // ─── 💡 Count pending for admin dashboard badge (PENDING_ADMIN သို့ ပြောင်းလဲပြီး) ─────────────
    @Query("SELECT COUNT(d) FROM ItemDonation d WHERE d.status = com.hnaungkyoe.entity.ItemDonation.Status.PENDING_ADMIN")
    long countPendingAdminReview();

}