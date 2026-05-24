package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.VolunteerApplication;
import com.hnaungkyoe.entity.User;
import com.hnaungkyoe.repository.VolunteerApplicationRepository;
import com.hnaungkyoe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/volunteers")
public class VolunteerAdminController {

    @Autowired
    private VolunteerApplicationRepository volunteerApplicationRepository;

    @Autowired
    private UserRepository userRepository;

    // ─────────────────────────────────────────────
    // VOLUNTEER LIST (APPROVED တွေ)
    // GET /api/admin/volunteers
    // ─────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<VolunteerApplication>> getVolunteers(
            @RequestParam(required = false, defaultValue = "") String search) {

        List<VolunteerApplication> volunteers;

        if (search.isEmpty()) {
            volunteers = volunteerApplicationRepository
                    .findByStatusOrderByAppliedAtDesc(VolunteerApplication.Status.APPROVED);  // ✅
        } else {
            volunteers = volunteerApplicationRepository
                    .findByStatusAndUser_UsernameContainingIgnoreCaseOrderByAppliedAtDesc(
                            VolunteerApplication.Status.APPROVED, search);  // ✅
        }

        return ResponseEntity.ok(volunteers);
    }

    // ─────────────────────────────────────────────
    // APPLICATIONS LIST (PENDING တွေ)
    // GET /api/admin/volunteers/applications
    // ─────────────────────────────────────────────
    @GetMapping("/applications")
    public ResponseEntity<List<VolunteerApplication>> getApplications(
            @RequestParam(required = false, defaultValue = "") String search) {

        List<VolunteerApplication> applications;

        if (search.isEmpty()) {
            applications = volunteerApplicationRepository
                    .findByStatusOrderByAppliedAtDesc(VolunteerApplication.Status.PENDING);  // ✅
        } else {
            applications = volunteerApplicationRepository
                    .findByStatusAndUser_UsernameContainingIgnoreCaseOrderByAppliedAtDesc(
                            VolunteerApplication.Status.PENDING, search);  // ✅
        }

        return ResponseEntity.ok(applications);
    }

    // ─────────────────────────────────────────────
    // APPROVE APPLICATION
    // PATCH /api/admin/volunteers/applications/{id}/approve
    // ─────────────────────────────────────────────
    @PatchMapping("/applications/{id}/approve")
    public ResponseEntity<VolunteerApplication> approveApplication(
            @PathVariable Long id) {

        VolunteerApplication app = volunteerApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        app.setStatus(VolunteerApplication.Status.APPROVED);  // ✅
        volunteerApplicationRepository.save(app);

        User user = app.getUser();  // ✅ was getApplicant()
        user.setRole(User.Role.ROLE_VOLUNTEER);
        userRepository.save(user);

        return ResponseEntity.ok(app);
    }

    // ─────────────────────────────────────────────
    // REJECT APPLICATION
    // PATCH /api/admin/volunteers/applications/{id}/reject
    // ─────────────────────────────────────────────
    @PatchMapping("/applications/{id}/reject")
    public ResponseEntity<VolunteerApplication> rejectApplication(
            @PathVariable Long id) {

        VolunteerApplication app = volunteerApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        app.setStatus(VolunteerApplication.Status.REJECTED);  // ✅
        volunteerApplicationRepository.save(app);

        return ResponseEntity.ok(app);
    }

    // ─────────────────────────────────────────────
    // FIRE VOLUNTEER (APPROVED → FIRED)
    // PATCH /api/admin/volunteers/{id}/fire
    // ─────────────────────────────────────────────
    @PatchMapping("/{id}/fire")
    public ResponseEntity<VolunteerApplication> fireVolunteer(
            @PathVariable Long id) {

        VolunteerApplication app = volunteerApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Volunteer record not found"));

        app.setStatus(VolunteerApplication.Status.FIRED);  // ✅
        volunteerApplicationRepository.save(app);

        User user = app.getUser();  // ✅ was getApplicant()
        user.setRole(User.Role.ROLE_PUBLIC);
        userRepository.save(user);

        return ResponseEntity.ok(app);
    }
}