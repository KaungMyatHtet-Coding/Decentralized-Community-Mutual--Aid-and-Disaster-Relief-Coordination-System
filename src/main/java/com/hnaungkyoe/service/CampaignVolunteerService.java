package com.hnaungkyoe.service;

import com.hnaungkyoe.entity.CampaignVolunteer;
import com.hnaungkyoe.repository.CampaignVolunteerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CampaignVolunteerService {
    private final CampaignVolunteerRepository repository;

    @Autowired
    public CampaignVolunteerService(CampaignVolunteerRepository repository) {
        this.repository = repository;
    }

    public CampaignVolunteer assignVolunteer(CampaignVolunteer cv) {
        return repository.save(cv);
    }

    public List<CampaignVolunteer> getAllAssignments() {
        return repository.findAll();
    }
}