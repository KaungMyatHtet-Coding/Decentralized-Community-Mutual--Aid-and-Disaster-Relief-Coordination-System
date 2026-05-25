package com.hnaungkyoe.service;

import com.hnaungkyoe.entity.Campaign;
import com.hnaungkyoe.repository.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CampaignService {
    private final CampaignRepository campaignRepository;

    @Autowired
    public CampaignService(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    // ✅ မူရင်း — မထိဘူး
    public Campaign createCampaign(Campaign campaign) {
        return campaignRepository.save(campaign);
    }

    // ✅ မူရင်း — မထိဘူး
    public List<Campaign> getAllCampaigns() {
        return campaignRepository.findAll();
    }

    // ✅ မူရင်း — မထိဘူး
    public Optional<Campaign> getCampaignById(Long id) {
        return campaignRepository.findById(id);
    }

    // ➕ အသစ် ထပ်ထည့်
    public Campaign updateCampaign(Long id, Campaign updated) {
        Campaign existing = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found: " + id));
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setTargetAmount(updated.getTargetAmount());
        existing.setStatus(updated.getStatus());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        return campaignRepository.save(existing);
    }

    // ➕ အသစ် ထပ်ထည့်
    public void deleteCampaign(Long id) {
        if (!campaignRepository.existsById(id)) {
            throw new RuntimeException("Campaign not found: " + id);
        }
        campaignRepository.deleteById(id);
    }
}