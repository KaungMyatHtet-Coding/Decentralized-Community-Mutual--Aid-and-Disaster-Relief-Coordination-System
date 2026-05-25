package com.hnaungkyoe.service;

import com.hnaungkyoe.entity.Campaign;
import com.hnaungkyoe.entity.Notification;
import com.hnaungkyoe.repository.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CampaignService {
    private final CampaignRepository campaignRepository;

    // ✅ ထည့်လိုက်
    @Autowired private NotificationService notificationService;


    @Autowired
    public CampaignService(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    public Campaign createCampaign(Campaign campaign) {
        Campaign saved = campaignRepository.save(campaign);

        // ✅ Users အားလုံးကို notify
        notificationService.sendToAllUsers(
                "📋 Campaign အသစ် စတင်ပြီ!",
                "'" + saved.getTitle() + "' campaign သို့ ဝင်ရောက်ကြည့်ရှုပါ။",
                Notification.Type.STATUS_CHANGED,
                saved.getId(),
                "CAMPAIGN"
        );

        return saved;
    }

    public Campaign updateCampaign(Long id, Campaign updated) {
        Campaign existing = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found: " + id));

        Campaign.Status oldStatus = existing.getStatus();

        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setTargetAmount(updated.getTargetAmount());
        existing.setStatus(updated.getStatus());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());

        Campaign saved = campaignRepository.save(existing);

        // ✅ Status ပြောင်းရင်သာ notify
        if (oldStatus != updated.getStatus()) {
            notificationService.sendToAllUsers(
                    "📋 Campaign Update",
                    "'" + saved.getTitle() + "' status: "
                            + oldStatus.name() + " → " + updated.getStatus().name(),
                    Notification.Type.STATUS_CHANGED,
                    saved.getId(),
                    "CAMPAIGN"
            );
        }

        return saved;
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
}