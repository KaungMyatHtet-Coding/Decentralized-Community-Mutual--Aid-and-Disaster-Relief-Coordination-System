package com.hnaungkyoe.service;

import com.hnaungkyoe.entity.VolunteerApplication;
import com.hnaungkyoe.repository.VolunteerApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class VolunteerApplicationService {
    private final VolunteerApplicationRepository repository;

    @Autowired
    public VolunteerApplicationService(VolunteerApplicationRepository repository) {
        this.repository = repository;
    }

    public VolunteerApplication apply(VolunteerApplication application) {
        return repository.save(application);
    }

    public List<VolunteerApplication> getAllApplications() {
        return repository.findAll();
    }

    public Optional<VolunteerApplication> getApplicationById(Long id) {
        return repository.findById(id);
    }
}