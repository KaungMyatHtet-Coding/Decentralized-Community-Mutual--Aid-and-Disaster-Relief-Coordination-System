package com.hnaungkyoe.repository;

import com.hnaungkyoe.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByCampaignId(Long campaignId);
    List<Donation> findByDonorId(Long donorId);
    List<Donation> findByStatus(Donation.Status status);

    @Query(value = "SELECT * FROM donations WHERE user_id = :userId", nativeQuery = true)
    List<Donation> findByDonorUserIdNative(@Param("userId") Long userId);

    List<Donation> findByDonor_Id(Long userId);

    @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.campaign.id = :campaignId AND d.status = 'CONFIRMED'")
    BigDecimal sumConfirmedAmountByCampaignId(@Param("campaignId") Long campaignId);

    // ✨ Filter queries အသစ်များ
    @Query("SELECT d FROM Donation d LEFT JOIN FETCH d.donor LEFT JOIN FETCH d.campaign " +
            "WHERE (:status IS NULL OR d.status = :status) " +
            "AND (:type IS NULL OR d.donationType = :type) " +
            "AND (:from IS NULL OR d.donatedAt >= :from) " +
            "AND (:to IS NULL OR d.donatedAt <= :to) " +
            "AND (:username IS NULL OR d.donor.username LIKE %:username%) " +
            "ORDER BY d.donatedAt DESC")
    List<Donation> filterDonations(
            @Param("status") Donation.Status status,
            @Param("type") Donation.DonationType type,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("username") String username
    );

    // ✨ Chart data — monthly totals
    @Query(value = """
            SELECT DATE_FORMAT(donated_at, '%Y-%m') as month,
                   SUM(amount) as total
            FROM donations
            WHERE status = 'CONFIRMED'
            AND donated_at >= DATE_SUB(NOW(), INTERVAL 6 MONTH)
            GROUP BY DATE_FORMAT(donated_at, '%Y-%m')
            ORDER BY month ASC
            """, nativeQuery = true)
    List<Object[]> getMonthlyDonationTotals();

    // ✨ Chart data — donation type breakdown
    @Query("SELECT d.donationType, COUNT(d), SUM(d.amount) FROM Donation d " +
            "WHERE d.status = 'CONFIRMED' GROUP BY d.donationType")
    List<Object[]> getDonationTypeStats();

    // ✨ Chart data — top campaigns
    @Query("SELECT d.campaign.title, SUM(d.amount) FROM Donation d " +
            "WHERE d.status = 'CONFIRMED' AND d.donationType = 'MONEY' " +
            "GROUP BY d.campaign.title ORDER BY SUM(d.amount) DESC")
    List<Object[]> getTopCampaignStats();
    long countByStatus(Donation.Status status);
}