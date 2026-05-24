package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.VolunteerApplication;
import com.hnaungkyoe.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VolunteerApplicationRepository volunteerApplicationRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // Total donations count
        stats.put("totalDonations", donationRepository.count());

        // Pending donations (PENDING status)
        stats.put("pendingDonations",
                donationRepository.countByStatus("PENDING"));

        // Active campaigns
        stats.put("activeCampaigns",
                campaignRepository.countByStatus("ACTIVE"));

        // Total registered users
        stats.put("totalUsers", userRepository.count());

        // ✅ Enum instead of String
        stats.put("pendingApplications",
                volunteerApplicationRepository.countByStatus(VolunteerApplication.Status.PENDING));

        // ✅ Enum instead of String
        stats.put("activeVolunteers",
                volunteerApplicationRepository.countByStatus(VolunteerApplication.Status.APPROVED));

        return ResponseEntity.ok(stats);
    }
}