package com.hnaungkyoe.service;

import com.hnaungkyoe.entity.*;
import com.hnaungkyoe.repository.CampaignRepository;
import com.hnaungkyoe.repository.DonationRepository;
import com.hnaungkyoe.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class DonationService {

    @Autowired private DonationRepository donationRepository;
    @Autowired private CampaignRepository campaignRepository;
    @Autowired private StockRepository stockRepository;
    @Autowired private NotificationService notificationService;

    public Donation recordDonation(Donation donation) {
        return donationRepository.save(donation);
    }

    @Transactional
    public Donation confirmDonation(Long donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation not found"));

        donation.setStatus(Donation.Status.CONFIRMED);

        // MONEY ဆိုရင် Campaign currentAmount တိုးတယ်
        if (donation.getDonationType() == Donation.DonationType.MONEY
                && donation.getAmount() != null) {
            Campaign campaign = donation.getCampaign();
            campaign.setCurrentAmount(
                    campaign.getCurrentAmount().add(donation.getAmount())
            );
            campaignRepository.save(campaign);
        }

        // ITEMS ဆိုရင် Stock တိုးတယ်
        if (donation.getDonationType() == Donation.DonationType.ITEMS
                && donation.getItemName() != null) {
            stockRepository.findByItemName(donation.getItemName())
                    .ifPresent(stock -> {
                        stock.setQuantity(stock.getQuantity() + donation.getQuantity());
                        stockRepository.save(stock);
                    });
        }

        Donation confirmed = donationRepository.save(donation);

        // Donor ကို notify လုပ်တယ်
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
}