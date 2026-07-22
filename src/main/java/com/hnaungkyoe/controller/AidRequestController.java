package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.AidRequest;
import com.hnaungkyoe.entity.User;
import com.hnaungkyoe.dto.ResolveRequestDto;
import com.hnaungkyoe.service.AidRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/aid-requests")
public class AidRequestController {

    @Autowired private AidRequestService service;

    // ✅ reporter ကို JWT ကနေ ယူတယ် — frontend က ပို့စရာမလို
    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody AidRequest request,
            @AuthenticationPrincipal User currentUser) {
        try {
            request.setReporter(currentUser);
            return ResponseEntity.ok(service.createAidRequest(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<AidRequest>> getAll(@AuthenticationPrincipal User currentUser) {
        if (currentUser != null && currentUser.getRole() == User.Role.ROLE_SUB_ADMIN) {
            return ResponseEntity.ok(service.getByTownship(currentUser.getTownship()));
        }
        return ResponseEntity.ok(service.getAllAidRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return service.getAidRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AidRequest>> getByStatus(
            @PathVariable AidRequest.Status status) {
        return ResponseEntity.ok(service.getByStatus(status));
    }

    @GetMapping("/township/{township}")
    public ResponseEntity<List<AidRequest>> getByTownship(
            @PathVariable String township) {
        return ResponseEntity.ok(service.getByTownship(township));
    }

    // ✅ adminId ကို JWT ကနေ ယူတယ်
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam AidRequest.Status status,
            @RequestParam(required = false) String proofPhotoUrl,
            @AuthenticationPrincipal User currentUser) {
        try {
            return ResponseEntity.ok(
                    service.updateStatus(id, status, currentUser.getId(), proofPhotoUrl));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<?> resolveRequest(
            @PathVariable Long id,
            @RequestBody ResolveRequestDto dto,
            @AuthenticationPrincipal User currentUser) {
        try {
            return ResponseEntity.ok(service.resolveRequest(id, dto, currentUser.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ✅ မိမိတင်ထားတဲ့ requests တွေ ကြည့်တယ်
    @GetMapping("/my")
    public ResponseEntity<List<AidRequest>> getMyRequests(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                service.getByReporterId(currentUser.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.deleteAidRequest(id);
        return ResponseEntity.ok("Aid request deleted successfully");
    }
    @GetMapping("/categories")
    public ResponseEntity<List<AidRequest>> getByCategories(
            @RequestParam List<AidRequest.Category> categories) {
        return ResponseEntity.ok(service.getByCategories(categories));
    }

    // ── Volunteer Endpoints ─────────────────────────────────

    @GetMapping("/volunteer/available")
    public ResponseEntity<?> getAvailableForVolunteer(@AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != User.Role.ROLE_VOLUNTEER) {
            return ResponseEntity.status(403).body("Only volunteers can access this endpoint.");
        }
        return ResponseEntity.ok(service.getAvailableForVolunteer(currentUser.getTownship()));
    }

    @GetMapping("/volunteer/my-assignments")
    public ResponseEntity<?> getMyAssignments(@AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != User.Role.ROLE_VOLUNTEER) {
            return ResponseEntity.status(403).body("Only volunteers can access this endpoint.");
        }
        return ResponseEntity.ok(service.getMyAssignments(currentUser.getId()));
    }

    @PostMapping("/{id}/volunteer-accept")
    public ResponseEntity<?> volunteerAcceptTask(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != User.Role.ROLE_VOLUNTEER) {
            return ResponseEntity.status(403).body("Only volunteers can accept tasks.");
        }
        try {
            return ResponseEntity.ok(service.acceptTask(id, currentUser.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}