package com.hnaungkyoe.controller;

import com.hnaungkyoe.dto.UserProfileDto;
import com.hnaungkyoe.entity.User;
import com.hnaungkyoe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class UserProfileController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(currentUser);
    }

    @PutMapping
    public ResponseEntity<?> updateMyProfile(
            @AuthenticationPrincipal User currentUser,
            @RequestBody UserProfileDto dto) {

        try {
            System.out.println("=== DTO RECEIVED FROM FRONTEND ===");
            System.out.println("Skills: " + dto.getSkills());
            System.out.println("AvailableDays: " + dto.getAvailableDays());
            System.out.println("AvailableTimes: " + dto.getAvailableTimes());
            System.out.println("YearsOfExperience: " + dto.getYearsOfExperience());
            System.out.println("VehicleType: " + dto.getVehicleType());

            // Personal Info
            if (dto.getFullName() != null) currentUser.setFullName(dto.getFullName());
            if (dto.getPhoneNumber() != null) currentUser.setPhoneNumber(dto.getPhoneNumber());
            if (dto.getNrc() != null) currentUser.setNrc(dto.getNrc());
            if (dto.getDateOfBirth() != null) currentUser.setDateOfBirth(dto.getDateOfBirth());
            if (dto.getGender() != null) {
                try {
                    currentUser.setGender(User.Gender.valueOf(dto.getGender().toUpperCase()));
                } catch (Exception e) {
                    System.out.println("Gender parse error: " + e.getMessage());
                }
            }
            if (dto.getProfilePhotoUrl() != null) currentUser.setProfilePhotoUrl(dto.getProfilePhotoUrl());

            // Location
            if (dto.getDivision() != null) currentUser.setDivision(dto.getDivision());
            if (dto.getTownship() != null) currentUser.setTownship(dto.getTownship());
            if (dto.getStreetAddress() != null) currentUser.setStreetAddress(dto.getStreetAddress());
            if (dto.getPostalCode() != null) currentUser.setPostalCode(dto.getPostalCode());

            // Volunteer Fields - Force set them
            currentUser.setHasVehicle(dto.getHasVehicle());
            currentUser.setVehicleType(dto.getVehicleType());
            currentUser.setEmergencyContactName(dto.getEmergencyContactName());
            currentUser.setEmergencyContactPhone(dto.getEmergencyContactPhone());

            // These are the ones not saving
            currentUser.setSkills(dto.getSkills());
            currentUser.setAvailableDays(dto.getAvailableDays());
            currentUser.setAvailableTimes(dto.getAvailableTimes());
            currentUser.setYearsOfExperience(dto.getYearsOfExperience());

            // Profile completion
            boolean isComplete = currentUser.getFullName() != null &&
                    currentUser.getPhoneNumber() != null &&
                    currentUser.getTownship() != null;
            currentUser.setProfileCompleted(isComplete);

            User saved = userRepository.save(currentUser);

            System.out.println("=== AFTER SAVE ===");
            System.out.println("Skills saved: " + saved.getSkills());
            System.out.println("Available Days saved: " + saved.getAvailableDays());
            System.out.println("Available Times saved: " + saved.getAvailableTimes());
            System.out.println("Years saved: " + saved.getYearsOfExperience());

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Update failed: " + e.getMessage());
        }
    }
}