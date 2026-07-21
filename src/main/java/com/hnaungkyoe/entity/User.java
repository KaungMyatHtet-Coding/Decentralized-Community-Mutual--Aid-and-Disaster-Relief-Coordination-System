package com.hnaungkyoe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─── Auth Fields ───────────────────────────────────────
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean verified = false;

    // ─── Personal Info ─────────────────────────────────────
    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "nrc", length = 30)
    private String nrc;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    // ─── Location ──────────────────────────────────────────
    @Column(name = "division", length = 100)
    private String division;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "township", length = 100)
    private String township;

    @Column(name = "street_address", columnDefinition = "TEXT")
    private String streetAddress;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    // ─── Volunteer Fields ──────────────────────────────────
    @Column(name = "has_vehicle")
    @Builder.Default
    private Boolean hasVehicle = false;

    @Column(name = "vehicle_type", length = 100)
    private String vehicleType;

    @Column(name = "emergency_contact_name", length = 100)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(name = "skills", length = 500)
    private String skills;

    @Column(name = "available_days", length = 100)
    private String availableDays;

    @Column(name = "available_times", length = 100)
    private String availableTimes;

    @Column(name = "years_of_experience")
    @Builder.Default
    private Integer yearsOfExperience = 0;

    // ─── Profile Status ────────────────────────────────────
    @Column(name = "profile_completed")
    @Builder.Default
    private Boolean profileCompleted = false;

    // ─── Volunteer Availability ────────────────────────────
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // ─── Timestamps ────────────────────────────────────────
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ─── Lifecycle ─────────────────────────────────────────
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.role == null) this.role = Role.ROLE_PUBLIC;
        if (this.verified == null) this.verified = false;
        if (this.hasVehicle == null) this.hasVehicle = false;
        if (this.profileCompleted == null) this.profileCompleted = false;
        if (this.yearsOfExperience == null) this.yearsOfExperience = 0;
        if (this.isActive == null) this.isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ─── Enums ─────────────────────────────────────────────
    public enum Role {
        ROLE_PUBLIC,
        ROLE_VOLUNTEER,
        ROLE_SENIOR_VOLUNTEER,
        ROLE_CITY_ADMIN,
        ROLE_DIVISION_ADMIN,
        ROLE_SUB_ADMIN,
        ROLE_SUPER_ADMIN
    }

    public enum Gender {
        MALE,
        FEMALE,
        OTHER
    }
}