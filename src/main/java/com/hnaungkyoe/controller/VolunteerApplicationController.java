package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.User;
import com.hnaungkyoe.entity.VolunteerApplication;
import com.hnaungkyoe.service.VolunteerApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/volunteer-applications")
@CrossOrigin(origins = "*")
public class VolunteerApplicationController {

    @Autowired private VolunteerApplicationService service;
    @PostMapping
    public ResponseEntity<?> apply(
            @RequestBody VolunteerApplication application,
            @AuthenticationPrincipal User currentUser) {
        try {
            application.setUser(currentUser); // ← JWT ကနေ ယူ
            return ResponseEntity.ok(service.apply(application));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<VolunteerApplication>> getAll() {
        return ResponseEntity.ok(service.getAllApplications());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<VolunteerApplication>> getPending() {
        return ResponseEntity.ok(service.getPendingApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return service.getApplicationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.approveApplication(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.rejectApplication(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}