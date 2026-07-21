package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.ItemDonation;
import com.hnaungkyoe.entity.VolunteerApplication;
import com.hnaungkyoe.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import com.hnaungkyoe.entity.Donation;
import com.hnaungkyoe.entity.Campaign;
import com.hnaungkyoe.entity.AidRequest;
import com.hnaungkyoe.entity.User;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

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

    @Autowired
    private AidRequestRepository aidRequestRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getStats(@AuthenticationPrincipal User currentUser) {
        Map<String, Object> stats = new HashMap<>();
        
        boolean isSubAdmin = currentUser != null && currentUser.getRole() == User.Role.ROLE_SUB_ADMIN;
        String township = isSubAdmin ? currentUser.getTownship() : null;

        stats.put("totalDonations",
                donationRepository.count() + itemDonationRepository.count()); // total doesn't matter much for subadmin

        if (isSubAdmin) {
            stats.put("pendingDonations", itemDonationRepository.countByStatusAndDonorTownship(ItemDonation.Status.ASSIGNED_TO_VOLUNTEER, township));
            stats.put("totalVolunteers", volunteerApplicationRepository.countByStatusAndUser_Township(VolunteerApplication.Status.APPROVED, township));
            stats.put("pendingAidRequests", aidRequestRepository.countByStatusAndTownship(AidRequest.Status.PENDING, township));
        } else {
            stats.put("pendingDonations",
                    donationRepository.countByStatus(Donation.Status.PENDING) +
                            itemDonationRepository.countByStatus(ItemDonation.Status.ASSIGNED_TO_VOLUNTEER));
            stats.put("totalVolunteers", volunteerApplicationRepository.countByStatus(VolunteerApplication.Status.APPROVED));
            stats.put("pendingAidRequests", aidRequestRepository.countByStatus(AidRequest.Status.PENDING));
        }

        stats.put("activeCampaigns",
                campaignRepository.countByStatus(Campaign.Status.ACTIVE));

        stats.put("totalUsers", userRepository.count());
        stats.put("pendingApplications",
                volunteerApplicationRepository.countByStatus(VolunteerApplication.Status.PENDING));
        stats.put("activeVolunteers",
                volunteerApplicationRepository.countByStatus(VolunteerApplication.Status.APPROVED));

        // Added for Dashboard exact keys
        stats.put("totalCampaigns", campaignRepository.countByStatus(Campaign.Status.ACTIVE));
        stats.put("totalAdmins", userRepository.countByRoleIn(List.of(User.Role.ROLE_SUPER_ADMIN, User.Role.ROLE_SUB_ADMIN)));

        return ResponseEntity.ok(stats);
    }
}