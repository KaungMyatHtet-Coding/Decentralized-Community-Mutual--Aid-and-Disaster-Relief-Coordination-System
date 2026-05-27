package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.ItemDonation;
import com.hnaungkyoe.entity.VolunteerApplication;
import com.hnaungkyoe.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import com.hnaungkyoe.entity.Donation;
import com.hnaungkyoe.entity.Campaign;

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

    @Autowired
    private ItemDonationRepository itemDonationRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalDonations",
                donationRepository.count() + itemDonationRepository.count());

        stats.put("pendingDonations",
                donationRepository.countByStatus(Donation.Status.PENDING) +
                        itemDonationRepository.countByStatus(ItemDonation.Status.ASSIGNED_TO_VOLUNTEER));  // ← ပြင်ထားပါတယ်

        stats.put("activeCampaigns",
                campaignRepository.countByStatus(Campaign.Status.ACTIVE));

        stats.put("totalUsers", userRepository.count());
        stats.put("pendingApplications",
                volunteerApplicationRepository.countByStatus(VolunteerApplication.Status.PENDING));
        stats.put("activeVolunteers",
                volunteerApplicationRepository.countByStatus(VolunteerApplication.Status.APPROVED));

        return ResponseEntity.ok(stats);
    }
}