package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.*;
import com.hnaungkyoe.service.ItemDonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/item-donations")
@CrossOrigin(origins = "*")
public class ItemDonationController {

    @Autowired private ItemDonationService service;

    // User — item donation တင်
    @PostMapping
    public ResponseEntity<?> submit(
            @RequestBody ItemDonation donation,
            @AuthenticationPrincipal User currentUser) {
        try {
            donation.setDonor(currentUser);
            return ResponseEntity.ok(service.submitItemDonation(donation));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Volunteer — received confirm
    @PatchMapping("/{id}/volunteer-confirm")
    public ResponseEntity<?> volunteerConfirm(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User currentUser) {
        try {
            return ResponseEntity.ok(service.volunteerConfirm(
                    id,
                    body.get("confirmPhotoUrl"),
                    body.get("note"),
                    currentUser
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Admin — store in stock
    @PatchMapping("/{id}/store")
    public ResponseEntity<?> store(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.storeInStock(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Admin — reject
    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.rejectItemDonation(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Admin — all item donations
    @GetMapping
    public ResponseEntity<List<ItemDonation>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // Admin — filter by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ItemDonation>> getByStatus(
            @PathVariable ItemDonation.Status status) {
        return ResponseEntity.ok(service.getByStatus(status));
    }

    // User — my donations
    @GetMapping("/my")
    public ResponseEntity<List<ItemDonation>> getMyDonations(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.getMyItemDonations(currentUser.getId()));
    }

    // Volunteer — assigned to me
    @GetMapping("/assigned")
    public ResponseEntity<List<ItemDonation>> getAssigned(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.getVolunteerAssigned(currentUser.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}