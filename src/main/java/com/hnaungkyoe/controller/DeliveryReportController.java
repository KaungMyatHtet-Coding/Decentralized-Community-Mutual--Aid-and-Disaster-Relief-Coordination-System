package com.hnaungkyoe.controller;

import com.hnaungkyoe.dto.DeliveryReportDto;
import com.hnaungkyoe.entity.DeliveryReport;
import com.hnaungkyoe.entity.User;
import com.hnaungkyoe.service.DeliveryReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery-reports")
public class DeliveryReportController {

    @Autowired private DeliveryReportService deliveryReportService;

    // ── Volunteer: Submit delivery report ─────────────────────────
    @PostMapping
    public ResponseEntity<?> submit(
            @RequestBody DeliveryReportDto dto,
            @AuthenticationPrincipal User currentUser) {
        try {
            return ResponseEntity.ok(deliveryReportService.submitReport(currentUser.getId(), dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── Volunteer: My submitted reports ──────────────────────────
    @GetMapping("/my")
    public ResponseEntity<List<DeliveryReport>> getMyReports(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(deliveryReportService.getMyReports(currentUser.getId()));
    }

    // ── Admin: Pending reports for their township ─────────────────
    @GetMapping("/pending")
    public ResponseEntity<?> getPending(@AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() == User.Role.ROLE_SUPER_ADMIN) {
            return ResponseEntity.ok(deliveryReportService.getAllReports());
        }
        return ResponseEntity.ok(
                deliveryReportService.getPendingForTownship(currentUser.getTownship()));
    }

    // ── Admin: All reports for their township ─────────────────────
    @GetMapping("/township")
    public ResponseEntity<?> getTownshipReports(@AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() == User.Role.ROLE_SUPER_ADMIN) {
            return ResponseEntity.ok(deliveryReportService.getAllReports());
        }
        return ResponseEntity.ok(
                deliveryReportService.getAllForTownship(currentUser.getTownship()));
    }

    // ── Admin: Approve → stock deduct + RESOLVED ─────────────────
    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        try {
            return ResponseEntity.ok(deliveryReportService.approveReport(id, currentUser.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── Admin: Reject → notify volunteer ─────────────────────────
    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> reject(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal User currentUser) {
        try {
            return ResponseEntity.ok(deliveryReportService.rejectReport(id, currentUser.getId(), reason));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
