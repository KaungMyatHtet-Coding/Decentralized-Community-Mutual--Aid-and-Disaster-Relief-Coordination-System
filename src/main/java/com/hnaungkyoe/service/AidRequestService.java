package com.hnaungkyoe.service;

import com.hnaungkyoe.entity.AidRequest;
import com.hnaungkyoe.entity.Notification;
import com.hnaungkyoe.entity.User;
import com.hnaungkyoe.entity.Stock;
import com.hnaungkyoe.entity.EmergencyUsageLog;
import com.hnaungkyoe.dto.ResolveRequestDto;
import com.hnaungkyoe.repository.AidRequestRepository;
import com.hnaungkyoe.repository.UserRepository;
import com.hnaungkyoe.repository.StockRepository;
import com.hnaungkyoe.repository.EmergencyUsageLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AidRequestService {

    @Autowired private AidRequestRepository aidRequestRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private UserRepository userRepository;
    @Autowired private StockRepository stockRepository;
    @Autowired private EmergencyUsageLogRepository emergencyUsageLogRepository;

    public AidRequest createAidRequest(AidRequest request) {
        if (request.getItems() != null) {
            for (com.hnaungkyoe.entity.AidRequestItem item : request.getItems()) {
                item.setAidRequest(request);
            }
        }
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

    public List<AidRequest> getAvailableForVolunteer(String township) {
        return aidRequestRepository.findByTownshipAndStatus(township, AidRequest.Status.VERIFIED);
    }

    public List<AidRequest> getMyAssignments(Long volunteerId) {
        return aidRequestRepository.findByAssignedVolunteerId(volunteerId);
    }

    public AidRequest acceptTask(Long id, Long volunteerId) {
        AidRequest request = aidRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aid Request not found"));

        if (request.getStatus() != AidRequest.Status.VERIFIED) {
            throw new RuntimeException("Only VERIFIED requests can be accepted by volunteers.");
        }

        User volunteer = userRepository.findById(volunteerId)
                .orElseThrow(() -> new RuntimeException("Volunteer not found"));

        if (!request.getTownship().equals(volunteer.getTownship())) {
            throw new RuntimeException("You can only accept requests in your own township.");
        }

        request.setAssignedVolunteer(volunteer);
        request.setStatus(AidRequest.Status.IN_PROGRESS);

        AidRequest updated = aidRequestRepository.save(request);

        // Notify Reporter
        notificationService.sendNotification(
                updated.getReporter(),
                "Volunteer Assigned",
                "စေတနာ့ဝန်ထမ်း '" + volunteer.getFullName() + "' မှ သင့်ရဲ့အကူအညီတောင်းခံချက်ကို လက်ခံလိုက်ပါပြီ။",
                Notification.Type.STATUS_CHANGED,
                updated.getId(),
                "AID_REQUEST"
        );

        // Notify Admins
        notificationService.sendToAllAdmins(
                "Volunteer Accepted Task",
                "Volunteer '" + volunteer.getFullName() + "' accepted Aid Request #" + updated.getId(),
                Notification.Type.STATUS_CHANGED,
                updated.getId(),
                "AID_REQUEST"
        );

        return updated;
    }

    // Status update — PENDING→VERIFIED→RESOLVED
    public AidRequest updateStatus(Long id, AidRequest.Status newStatus, Long adminId, String proofPhotoUrl) {
        AidRequest request = aidRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aid Request not found"));

        if (request.getReporter() != null && request.getReporter().getId().equals(adminId)) {
            throw new RuntimeException("Conflict of Interest: You cannot approve or update your own Aid Request.");
        }

        AidRequest.Status oldStatus = request.getStatus();
        request.setStatus(newStatus);

        if (newStatus == AidRequest.Status.VERIFIED) {
            User admin = userRepository.findById(adminId)
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            request.setVerifiedBy(admin);
        }
        
        if (newStatus == AidRequest.Status.RESOLVED && proofPhotoUrl != null && !proofPhotoUrl.isEmpty()) {
            request.setProofPhotoUrl(proofPhotoUrl);
        }

        AidRequest updated = aidRequestRepository.save(request);

        // ✅ Reporter (user) ကို notify — status ပြောင်းကြောင်း
        notificationService.sendNotification(
                updated.getReporter(),
                "Aid Request " + newStatus.name(),
                "မင်းရဲ့ '" + updated.getTitle() + "' request status: "
                        + oldStatus.name() + " → " + newStatus.name(),
                Notification.Type.STATUS_CHANGED,
                updated.getId(),
                "AID_REQUEST"
        );

        // ✅ Admin တွေကိုလည်း notify — status ပြောင်းကြောင်း
        notificationService.sendToAllAdmins(
                "🆘 Aid Request Updated",
                "Request '" + updated.getTitle() + "' status changed: "
                        + oldStatus.name() + " → " + newStatus.name(),
                Notification.Type.STATUS_CHANGED,
                updated.getId(),
                "AID_REQUEST"
        );

        // ✅ VERIFIED ဖြစ်သွားရင် သက်ဆိုင်ရာ Township က Volunteer တွေကို အကြောင်းကြားမယ်
        if (newStatus == AidRequest.Status.VERIFIED) {
            notificationService.sendToVolunteersInTownship(
                    updated.getTownship(),
                    "📢 New Aid Request Available",
                    "'" + updated.getTitle() + "' အကူအညီတောင်းခံချက်ကို Admin မှ အတည်ပြုလိုက်ပါပြီ။ ဝင်ရောက်ကူညီပေးနိုင်ပါတယ်။",
                    Notification.Type.STATUS_CHANGED,
                    updated.getId(),
                    "AID_REQUEST"
            );
        }

        return updated;
    }

    public AidRequest resolveRequest(Long id, ResolveRequestDto dto, Long adminId) {
        AidRequest request = aidRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aid Request not found"));

        if (request.getStatus() == AidRequest.Status.RESOLVED) {
            throw new RuntimeException("Request is already resolved.");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        // Process deductions
        if (dto.getDeductions() != null && !dto.getDeductions().isEmpty()) {
            for (ResolveRequestDto.StockDeduction deduction : dto.getDeductions()) {
                Stock stock = stockRepository.findById(deduction.getStockId())
                        .orElseThrow(() -> new RuntimeException("Stock item not found: " + deduction.getStockId()));
                
                if (stock.getQuantity() < deduction.getQuantity()) {
                    throw new RuntimeException("Insufficient stock for item: " + stock.getItemName());
                }

                stock.setQuantity(stock.getQuantity() - deduction.getQuantity());
                stockRepository.save(stock);

                EmergencyUsageLog log = EmergencyUsageLog.builder()
                        .itemName(stock.getItemName())
                        .township(request.getTownship())
                        .quantityUsed(deduction.getQuantity())
                        .reasonDetails("Aid Request #" + request.getId() + " - " + request.getTitle())
                        .proofPhotoUrl(dto.getProofPhotoUrl())
                        .authorizedBy(admin.getUsername())
                        .build();
                emergencyUsageLogRepository.save(log);
            }
        }

        return updateStatus(id, AidRequest.Status.RESOLVED, adminId, dto.getProofPhotoUrl());
    }

    public void deleteAidRequest(Long id) {
        aidRequestRepository.deleteById(id);
    }
    public List<AidRequest> getByReporterId(Long reporterId) {
        return aidRequestRepository.findByReporterId(reporterId);
    }
    public List<AidRequest> getByCategories(List<AidRequest.Category> categories) {
        return aidRequestRepository.findByCategoriesIn(categories);
    }
}