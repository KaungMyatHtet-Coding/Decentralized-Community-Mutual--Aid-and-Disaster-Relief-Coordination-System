package com.hnaungkyoe.service;

import com.hnaungkyoe.entity.AidRequest;
import com.hnaungkyoe.repository.AidRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AidRequestService {
    private final AidRequestRepository aidRequestRepository;

    @Autowired
    public AidRequestService(AidRequestRepository aidRequestRepository) {
        this.aidRequestRepository = aidRequestRepository;
    }

    public AidRequest createAidRequest(AidRequest request) {
        return aidRequestRepository.save(request);
    }

    public List<AidRequest> getAllAidRequests() {
        return aidRequestRepository.findAll();
    }

    public Optional<AidRequest> getAidRequestById(Long id) {
        return aidRequestRepository.findById(id);
    }

    public void deleteAidRequest(Long id) {
        aidRequestRepository.deleteById(id);
    }
}