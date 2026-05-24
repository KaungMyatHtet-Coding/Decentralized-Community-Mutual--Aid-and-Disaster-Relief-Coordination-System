package com.hnaungkyoe.service;

import com.hnaungkyoe.entity.*;
import com.hnaungkyoe.repository.CampaignRepository;
import com.hnaungkyoe.repository.DonationRepository;
import com.hnaungkyoe.repository.StockRepository;
import com.hnaungkyoe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DonationService {

    @Autowired private DonationRepository donationRepository;
    @Autowired private CampaignRepository campaignRepository;
    @Autowired private StockRepository stockRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private UserRepository userRepository;

    public Donation recordDonation(Donation donation) {
        return donationRepository.save(donation);
    }

    @Transactional
    public Donation confirmDonation(Long donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation not found"));
        donation.setStatus(Donation.Status.CONFIRMED);

        if (donation.getDonationType() == Donation.DonationType.MONEY
                && donation.getAmount() != null) {
            Campaign campaign = donation.getCampaign();
            campaign.setCurrentAmount(campaign.getCurrentAmount().add(donation.getAmount()));
            campaignRepository.save(campaign);
        }

        if (donation.getDonationType() == Donation.DonationType.ITEMS
                && donation.getItemName() != null) {
            stockRepository.findByItemName(donation.getItemName())
                    .ifPresent(stock -> {
                        stock.setQuantity(stock.getQuantity() + donation.getQuantity());
                        stockRepository.save(stock);
                    });
        }

        Donation confirmed = donationRepository.save(donation);

        if (confirmed.getDonor() != null) {
            notificationService.sendNotification(
                    confirmed.getDonor(),
                    "Donation Confirmed",
                    "မင်းရဲ့ လှူဒါန်းမှု အတည်ပြုပြီ — ကျေးဇူးတင်ပါတယ်",
                    Notification.Type.DONATION_RECEIVED,
                    confirmed.getId(),
                    "DONATION"
            );
        }
        return confirmed;
    }

    public Donation rejectDonation(Long donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation not found"));
        donation.setStatus(Donation.Status.REJECTED);
        return donationRepository.save(donation);
    }

    public List<Donation> getAllDonations() {
        return donationRepository.findAll();
    }

    public Optional<Donation> getDonationById(Long id) {
        return donationRepository.findById(id);
    }

    public List<Donation> getByCampaignId(Long campaignId) {
        return donationRepository.findByCampaignId(campaignId);
    }

    public List<Donation> getPendingDonations() {
        return donationRepository.findByStatus(Donation.Status.PENDING);
    }

    public List<Donation> getMyDonationsHistory() {
        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof User currentUser) {
            return donationRepository.findByDonorUserIdNative(currentUser.getId());
        }
        return Collections.emptyList();
    }

    // ✨ Admin filter
    public List<Donation> filterDonations(String status, String type,
                                          String from, String to, String username) {
        Donation.Status statusEnum = (status != null && !status.isEmpty())
                ? Donation.Status.valueOf(status) : null;
        Donation.DonationType typeEnum = (type != null && !type.isEmpty())
                ? Donation.DonationType.valueOf(type) : null;
        LocalDateTime fromDate = (from != null && !from.isEmpty())
                ? LocalDate.parse(from).atStartOfDay() : null;
        LocalDateTime toDate = (to != null && !to.isEmpty())
                ? LocalDate.parse(to).atTime(23, 59, 59) : null;
        String usernameParam = (username != null && !username.isEmpty()) ? username : null;

        return donationRepository.filterDonations(statusEnum, typeEnum, fromDate, toDate, usernameParam);
    }

    // ✨ Stats for charts
    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();

        // Summary cards
        List<Donation> all = donationRepository.findAll();
        stats.put("totalDonations", all.size());
        stats.put("totalConfirmed", all.stream()
                .filter(d -> d.getStatus() == Donation.Status.CONFIRMED).count());
        stats.put("totalPending", all.stream()
                .filter(d -> d.getStatus() == Donation.Status.PENDING).count());
        stats.put("totalAmount", all.stream()
                .filter(d -> d.getStatus() == Donation.Status.CONFIRMED
                        && d.getDonationType() == Donation.DonationType.MONEY
                        && d.getAmount() != null)
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Monthly chart
        List<Object[]> monthly = donationRepository.getMonthlyDonationTotals();
        List<Map<String, Object>> monthlyList = new ArrayList<>();
        for (Object[] row : monthly) {
            Map<String, Object> m = new HashMap<>();
            m.put("month", row[0]);
            m.put("total", row[1]);
            monthlyList.add(m);
        }
        stats.put("monthlyData", monthlyList);

        // Type breakdown (pie chart)
        List<Object[]> typeStats = donationRepository.getDonationTypeStats();
        List<Map<String, Object>> typeList = new ArrayList<>();
        for (Object[] row : typeStats) {
            Map<String, Object> m = new HashMap<>();
            m.put("type", row[0]);
            m.put("count", row[1]);
            m.put("total", row[2]);
            typeList.add(m);
        }
        stats.put("typeData", typeList);

        // Top campaigns (bar chart)
        List<Object[]> campaignStats = donationRepository.getTopCampaignStats();
        List<Map<String, Object>> campaignList = new ArrayList<>();
        for (Object[] row : campaignStats) {
            Map<String, Object> m = new HashMap<>();
            m.put("campaign", row[0]);
            m.put("total", row[1]);
            campaignList.add(m);
        }
        stats.put("campaignData", campaignList);

        return stats;
    }
}