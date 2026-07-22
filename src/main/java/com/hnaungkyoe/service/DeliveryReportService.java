package com.hnaungkyoe.service;

import com.hnaungkyoe.dto.DeliveryReportDto;
import com.hnaungkyoe.entity.*;
import com.hnaungkyoe.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryReportService {

    @Autowired private DeliveryReportRepository deliveryReportRepository;
    @Autowired private AidRequestRepository aidRequestRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private StockRepository stockRepository;
    @Autowired private NotificationService notificationService;

    // ── Volunteer submits delivery report ────────────────────────
    @Transactional
    public DeliveryReport submitReport(Long volunteerId, DeliveryReportDto dto) {
        AidRequest aidRequest = aidRequestRepository.findById(dto.getAidRequestId())
                .orElseThrow(() -> new RuntimeException("Aid Request not found"));

        User volunteer = userRepository.findById(volunteerId)
                .orElseThrow(() -> new RuntimeException("Volunteer not found"));

        // Security: only assigned volunteer can submit
        if (aidRequest.getAssignedVolunteer() == null ||
            !aidRequest.getAssignedVolunteer().getId().equals(volunteerId)) {
            throw new RuntimeException("Only the assigned volunteer can submit a delivery report.");
        }

        if (aidRequest.getStatus() != AidRequest.Status.IN_PROGRESS) {
            throw new RuntimeException("Delivery report can only be submitted for IN_PROGRESS requests.");
        }

        // Check if already submitted
        deliveryReportRepository.findByAidRequestIdAndVolunteerId(dto.getAidRequestId(), volunteerId)
                .ifPresent(r -> {
                    if (r.getStatus() == DeliveryReport.Status.PENDING_REVIEW) {
                        throw new RuntimeException("You already submitted a report for this request. Waiting for admin review.");
                    }
                });

        // Map DTO → embedded entries
        List<DeliveryReport.DeliveredItemEntry> entries = dto.getDeliveredItems().stream()
                .map(d -> DeliveryReport.DeliveredItemEntry.builder()
                        .itemName(d.getItemName())
                        .quantityDelivered(d.getQuantityDelivered())
                        .unit(d.getUnit())
                        .build())
                .collect(Collectors.toList());

        DeliveryReport report = DeliveryReport.builder()
                .aidRequest(aidRequest)
                .volunteer(volunteer)
                .deliveredItems(entries)
                .proofPhotoUrl(dto.getProofPhotoUrl())
                .notes(dto.getNotes())
                .status(DeliveryReport.Status.PENDING_REVIEW)
                .build();

        DeliveryReport saved = deliveryReportRepository.save(report);

        // Notify Sub Admin of this township
        notificationService.sendToSubAdminsInTownship(
                aidRequest.getTownship(),
                "📦 Delivery Report Submitted",
                "Volunteer '" + volunteer.getFullName() + "' has submitted a delivery report for '" +
                        aidRequest.getTitle() + "'. Please review.",
                Notification.Type.STATUS_CHANGED,
                saved.getId(),
                "DELIVERY_REPORT"
        );

        return saved;
    }

    // ── Admin approves report → stock deduct + RESOLVED ──────────
    @Transactional
    public DeliveryReport approveReport(Long reportId, Long adminId) {
        DeliveryReport report = deliveryReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Delivery Report not found"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (report.getStatus() != DeliveryReport.Status.PENDING_REVIEW) {
            throw new RuntimeException("Report has already been reviewed.");
        }

        // Mark report approved
        report.setStatus(DeliveryReport.Status.APPROVED);
        report.setReviewedBy(admin);
        report.setReviewedAt(LocalDateTime.now());
        deliveryReportRepository.save(report);

        // Deduct stock for each delivered item (fuzzy & category-based matching)
        AidRequest aidRequest = report.getAidRequest();
        String township = aidRequest.getTownship() != null ? aidRequest.getTownship() : "Yangon";
        List<Stock> townshipStocks = stockRepository.findByTownship(township);

        for (DeliveryReport.DeliveredItemEntry item : report.getDeliveredItems()) {
            if (item.getQuantityDelivered() <= 0) continue;

            String rawName = item.getItemName() != null ? item.getItemName() : "";
            String cleanName = rawName.replaceAll("[^a-zA-Z0-9 ]", "").trim().toLowerCase();

            // 1. Try exact or partial name match
            Stock match = townshipStocks.stream()
                    .filter(s -> {
                        String sName = s.getItemName().toLowerCase();
                        return sName.equals(cleanName) || sName.contains(cleanName) || cleanName.contains(sName);
                    })
                    .findFirst()
                    .orElse(null);

            // 2. Fallback: Category-based match (FOOD, MEDICINE, WATER, CLOTHING, OTHERS)
            if (match == null) {
                Stock.Category targetCat = Stock.Category.OTHER;
                if (cleanName.contains("food") || cleanName.contains("rice") || cleanName.contains("ဆန်") || rawName.contains("🍚")) {
                    targetCat = Stock.Category.FOOD;
                } else if (cleanName.contains("med") || cleanName.contains("ဆေး") || rawName.contains("💊")) {
                    targetCat = Stock.Category.MEDICINE;
                } else if (cleanName.contains("water") || cleanName.contains("ရေ") || rawName.contains("💧")) {
                    targetCat = Stock.Category.WATER;
                } else if (cleanName.contains("cloth") || cleanName.contains("အဝတ်") || cleanName.contains("blanket") || rawName.contains("🧥")) {
                    targetCat = Stock.Category.CLOTHING;
                }

                final Stock.Category cat = targetCat;
                match = townshipStocks.stream()
                        .filter(s -> s.getCategory() == cat)
                        .findFirst()
                        .orElse(null);
            }

            // 3. Deduct stock if matched
            if (match != null) {
                double newQty = Math.max(0, match.getQuantity() - item.getQuantityDelivered());
                match.setQuantity(newQty);
                match.setUpdatedAt(LocalDateTime.now());
                stockRepository.save(match);
            }
        }

        // Mark Aid Request as RESOLVED
        aidRequest.setStatus(AidRequest.Status.RESOLVED);
        aidRequest.setProofPhotoUrl(report.getProofPhotoUrl());
        aidRequestRepository.save(aidRequest);

        // Notify volunteer
        notificationService.sendNotification(
                report.getVolunteer(),
                "✅ Delivery Approved",
                "Admin has approved your delivery report for '" + aidRequest.getTitle() +
                        "'. Thank you for your service! 🙏",
                Notification.Type.STATUS_CHANGED,
                report.getId(),
                "DELIVERY_REPORT"
        );

        // Notify reporter
        notificationService.sendNotification(
                aidRequest.getReporter(),
                "✅ Aid Request Resolved",
                "Your request '" + aidRequest.getTitle() + "' has been fulfilled by a volunteer. It is now marked as Resolved.",
                Notification.Type.STATUS_CHANGED,
                aidRequest.getId(),
                "AID_REQUEST"
        );

        return report;
    }

    // ── Admin rejects report → volunteer can resubmit ────────────
    @Transactional
    public DeliveryReport rejectReport(Long reportId, Long adminId, String reason) {
        DeliveryReport report = deliveryReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Delivery Report not found"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (report.getStatus() != DeliveryReport.Status.PENDING_REVIEW) {
            throw new RuntimeException("Report has already been reviewed.");
        }

        report.setStatus(DeliveryReport.Status.REJECTED);
        report.setReviewedBy(admin);
        report.setReviewedAt(LocalDateTime.now());
        report.setRejectReason(reason != null ? reason : "No reason provided.");
        deliveryReportRepository.save(report);

        // Notify volunteer
        notificationService.sendNotification(
                report.getVolunteer(),
                "❌ Delivery Report Rejected",
                "Your delivery report for '" + report.getAidRequest().getTitle() +
                        "' was rejected. Reason: " + report.getRejectReason() +
                        ". Please resubmit with correct information.",
                Notification.Type.STATUS_CHANGED,
                report.getId(),
                "DELIVERY_REPORT"
        );

        return report;
    }

    // ── Queries ───────────────────────────────────────────────────
    public List<DeliveryReport> getPendingForTownship(String township) {
        return deliveryReportRepository.findByAidRequest_TownshipAndStatus(
                township, DeliveryReport.Status.PENDING_REVIEW);
    }

    public List<DeliveryReport> getAllForTownship(String township) {
        return deliveryReportRepository.findByAidRequest_Township(township);
    }

    public List<DeliveryReport> getMyReports(Long volunteerId) {
        return deliveryReportRepository.findByVolunteerId(volunteerId);
    }

    public List<DeliveryReport> getAllReports() {
        return deliveryReportRepository.findAll();
    }
}
