package com.hnaungkyoe.service;

import com.hnaungkyoe.entity.Campaign;
import com.hnaungkyoe.entity.Notification;
import com.hnaungkyoe.repository.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;
import com.hnaungkyoe.entity.User;

@Service
public class CampaignService {
    private final CampaignRepository campaignRepository;

    // ✅ ထည့်လိုက်
    @Autowired private NotificationService notificationService;


    @Autowired
    public CampaignService(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    public Campaign createCampaign(Campaign campaign, User currentUser) {
        campaign.setAuthorId(currentUser.getId());
        if (campaign.getCurrentAmount() == null) {
            campaign.setCurrentAmount(java.math.BigDecimal.ZERO);
        }
        if (currentUser.getRole() == User.Role.ROLE_SUB_ADMIN) {
            campaign.setTownship(currentUser.getTownship());
            campaign.setStatus(Campaign.Status.PENDING);
        } else {
            campaign.setStatus(Campaign.Status.ACTIVE);
        }

        Campaign saved = campaignRepository.save(campaign);

        // ✅ Notify only if ACTIVE
        if (saved.getStatus() == Campaign.Status.ACTIVE) {
            notificationService.sendToAllUsers(
                    "📋 Campaign အသစ် စတင်ပြီ!",
                    "'" + saved.getTitle() + "' campaign သို့ ဝင်ရောက်ကြည့်ရှုပါ။",
                    Notification.Type.STATUS_CHANGED,
                    saved.getId(),
                    "CAMPAIGN"
            );
        }

        return saved;
    }

    public Campaign updateCampaign(Long id, Campaign updated, User currentUser) {
        Campaign existing = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found: " + id));

        Campaign.Status oldStatus = existing.getStatus();

        existing.setTitle(updated.getTitle());
        existing.setTitleMy(updated.getTitleMy());
        existing.setDescription(updated.getDescription());
        existing.setDescriptionMy(updated.getDescriptionMy());
        existing.setTargetAmount(updated.getTargetAmount());
        existing.setImageUrl(updated.getImageUrl());
        existing.setCategory(updated.getCategory());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());

        if (currentUser.getRole() == User.Role.ROLE_SUB_ADMIN) {
            existing.setStatus(Campaign.Status.PENDING);
        } else if (updated.getStatus() != null) {
            existing.setStatus(updated.getStatus());
        }

        Campaign saved = campaignRepository.save(existing);

        // ✅ Status ပြောင်းရင်သာ notify
        if (oldStatus != saved.getStatus() && saved.getStatus() == Campaign.Status.ACTIVE) {
            notificationService.sendToAllUsers(
                    "📋 Campaign Update",
                    "'" + saved.getTitle() + "' status: "
                            + oldStatus.name() + " → " + saved.getStatus().name(),
                    Notification.Type.STATUS_CHANGED,
                    saved.getId(),
                    "CAMPAIGN"
            );
        }

        return saved;
    }

    public Campaign approveCampaign(Long id) {
        Campaign existing = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found: " + id));
        existing.setStatus(Campaign.Status.ACTIVE);
        Campaign saved = campaignRepository.save(existing);
        notificationService.sendToAllUsers(
                "📋 Campaign Approved!",
                "'" + saved.getTitle() + "' is now active.",
                Notification.Type.STATUS_CHANGED,
                saved.getId(),
                "CAMPAIGN"
        );
        return saved;
    }

    public Campaign rejectCampaign(Long id) {
        Campaign existing = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found: " + id));
        existing.setStatus(Campaign.Status.REJECTED);
        return campaignRepository.save(existing);
    }

    // ဒါတွေ မပြောင်းဘူး
    public List<Campaign> getAllCampaigns() {
        return campaignRepository.findAll();
    }

    public Optional<Campaign> getCampaignById(Long id) {
        return campaignRepository.findById(id);
    }

    public void deleteCampaign(Long id) {
        if (!campaignRepository.existsById(id)) {
            throw new RuntimeException("Campaign not found: " + id);
        }
        campaignRepository.deleteById(id);
    }

    // Run every day at midnight to check for expired campaigns
    @Scheduled(cron = "0 0 0 * * ?")
    public void autoExpireCampaigns() {
        List<Campaign> activeCampaigns = campaignRepository.findAll().stream()
                .filter(c -> c.getStatus() == Campaign.Status.ACTIVE)
                .filter(c -> c.getEndDate().isBefore(LocalDateTime.now()))
                .toList();

        for (Campaign c : activeCampaigns) {
            c.setStatus(Campaign.Status.COMPLETED);
            campaignRepository.save(c);
        }
    }
}