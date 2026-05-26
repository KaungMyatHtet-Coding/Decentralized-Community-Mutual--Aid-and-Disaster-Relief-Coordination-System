package com.hnaungkyoe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private String fullName;
    private String phoneNumber;
    private String nrc;
    private LocalDate dateOfBirth;
    private String gender;
    private String profilePhotoUrl;

    private String division;
    private String city;
    private String township;
    private String streetAddress;
    private String postalCode;

    private Boolean hasVehicle;
    private String vehicleType;
    private String emergencyContactName;
    private String emergencyContactPhone;

    private String skills;
    private String availableDays;
    private String availableTimes;
    private Integer yearsOfExperience;
}