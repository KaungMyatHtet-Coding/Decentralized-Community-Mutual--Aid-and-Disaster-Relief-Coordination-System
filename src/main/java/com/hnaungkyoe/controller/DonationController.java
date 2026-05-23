package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.Donation;
import com.hnaungkyoe.service.DonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/donations")
@CrossOrigin(origins = "*")
public class DonationController {

    @Autowired private DonationService service;

    @PostMapping
    public ResponseEntity<?> record(@RequestBody Donation donation) {
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
    public ResponseEntity<?> confirm(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.confirmDonation(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.rejectDonation(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}