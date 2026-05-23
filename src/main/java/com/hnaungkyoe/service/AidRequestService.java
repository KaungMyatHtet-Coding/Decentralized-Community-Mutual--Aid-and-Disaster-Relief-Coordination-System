package com.hnaungkyoe.service;

import com.hnaungkyoe.entity.AidRequest;
import com.hnaungkyoe.entity.Notification;
import com.hnaungkyoe.entity.User;
import com.hnaungkyoe.repository.AidRequestRepository;
import com.hnaungkyoe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AidRequestService {

    @Autowired private AidRequestRepository aidRequestRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private UserRepository userRepository;

    public AidRequest createAidRequest(AidRequest request) {
        AidRequest saved = aidRequestRepository.save(request);
        // Notification ပို့တယ်
        notificationService.sendNotification(
                saved.getReporter(),
                "Aid Request Submitted",
                "မင်းရဲ့ အကူအညီတောင်းခံချက် တင်သွားပြီ — " + saved.getTitle(),
                Notification.Type.REQUEST_CREATED,
                saved.getId(),
                "AID_REQUEST"
        );
        return saved;
    }

    public List<AidRequest> getAllAidRequests() {
        return aidRequestRepository.findAll();
    }

    public Optional<AidRequest> getAidRequestById(Long id) {
        return aidRequestRepository.findById(id);
    }

    public List<AidRequest> getByStatus(AidRequest.Status status) {
        return aidRequestRepository.findByStatus(status);
    }

    public List<AidRequest> getByTownship(String township) {
        return aidRequestRepository.findByTownship(township);
    }

    // Status update — PENDING→VERIFIED→RESOLVED
    public AidRequest updateStatus(Long id, AidRequest.Status newStatus, Long adminId) {
        AidRequest request = aidRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aid Request not found"));

        AidRequest.Status oldStatus = request.getStatus();
        request.setStatus(newStatus);

        if (newStatus == AidRequest.Status.VERIFIED) {
            User admin = userRepository.findById(adminId)
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            request.setVerifiedBy(admin);
        }

        AidRequest updated = aidRequestRepository.save(request);

        // Reporter ကို notify လုပ်တယ်
        notificationService.sendNotification(
                updated.getReporter(),
                "Request Status Updated",
                "မင်းရဲ့ request status " + oldStatus + " မှ " + newStatus + " ပြောင်းသွားပြီ",
                Notification.Type.STATUS_CHANGED,
                updated.getId(),
                "AID_REQUEST"
        );

        return updated;
    }

    public void deleteAidRequest(Long id) {
        aidRequestRepository.deleteById(id);
    }
}