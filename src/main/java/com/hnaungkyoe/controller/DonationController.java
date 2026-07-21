package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.Donation;
import com.hnaungkyoe.entity.User;
import com.hnaungkyoe.service.DonationService;
import com.hnaungkyoe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/donations")
@CrossOrigin(origins = "*")
public class DonationController {

    @Autowired private DonationService service;
    @Autowired private UserRepository userRepository;

   

    @PostMapping
    public ResponseEntity<?> record(
            @RequestBody Donation donation,
            @AuthenticationPrincipal User currentUser) {
        donation.setDonor(currentUser);
        return ResponseEntity.ok(service.recordDonation(donation));
    }

    @GetMapping
    public ResponseEntity<List<Donation>> getAll() {
        return ResponseEntity.ok(service.getAllDonations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return service.getDonationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Donation>> getPending() {
        return ResponseEntity.ok(service.getPendingDonations());
    }

    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<List<Donation>> getByCampaign(@PathVariable Long campaignId) {
        return ResponseEntity.ok(service.getByCampaignId(campaignId));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        try { return ResponseEntity.ok(service.confirmDonation(id, currentUser.getId())); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        try { return ResponseEntity.ok(service.rejectDonation(id, currentUser.getId())); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @GetMapping("/my")
    public ResponseEntity<List<Donation>> getMyDonations() {
        return ResponseEntity.ok(service.getMyDonationsHistory());
    }

    // ✨ Admin filter endpoint
    @GetMapping("/filter")
    public ResponseEntity<List<Donation>> filter(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String username
    ) {
        return ResponseEntity.ok(service.filterDonations(status, type, from, to, username));
    }

    // ✨ Admin stats endpoint
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(service.getAdminStats());
    }
}