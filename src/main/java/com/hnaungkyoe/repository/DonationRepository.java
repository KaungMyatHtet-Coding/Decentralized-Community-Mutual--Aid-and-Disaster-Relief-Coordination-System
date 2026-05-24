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
    // ✨ အပိုင်ပိတ်ဆို့လိုက်သော Native Query: user_id ကော်လံကို တိုက်ရိုက်စစ်ထုတ်ရှာဖွေခြင်း ✨
    @Query(value = "SELECT * FROM donations WHERE user_id = :userId", nativeQuery = true)
    List<Donation> findByDonorUserIdNative(@Param("userId") Long userId);

    @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.campaign.id = :campaignId AND d.status = 'CONFIRMED'")
    BigDecimal sumConfirmedAmountByCampaignId(@Param("campaignId") Long campaignId);
    // ❌ စောစောက Email စာကြောင်းကို ဖျက်ပြီး...
    // ✨ အသစ်ပြောင်းလဲမည့်အချက်: Donor ရဲ့ ID အလိုက် ကွက်တိ စစ်ထုတ်ရှာဖွေခြင်း ✨
    List<Donation> findByDonor_Id(Long userId);
}