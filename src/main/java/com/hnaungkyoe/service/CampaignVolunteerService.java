package com.hnaungkyoe.service;

import com.hnaungkyoe.entity.CampaignVolunteer;
import com.hnaungkyoe.repository.CampaignVolunteerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CampaignVolunteerService {

    @Autowired private CampaignVolunteerRepository repository;

    public CampaignVolunteer assignVolunteer(CampaignVolunteer cv) {
        // Duplicate assign မဖြစ်အောင် စစ်တယ်
        if (repository.existsByCampaignIdAndUserId(
                cv.getCampaign().getId(), cv.getUser().getId())) {
            throw new RuntimeException("Volunteer already assigned to this campaign!");
        }
        return repository.save(cv);
    }

    public CampaignVolunteer updateStatus(Long id, CampaignVolunteer.Status status) {
        CampaignVolunteer cv = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        cv.setStatus(status);
        return repository.save(cv);
    }

    public List<CampaignVolunteer> getAllAssignments() {
        return repository.findAll();
    }

    public List<CampaignVolunteer> getByCampaignId(Long campaignId) {
        return repository.findByCampaignId(campaignId);
    }

    public void removeAssignment(Long id) {
        repository.deleteById(id);
    }
}