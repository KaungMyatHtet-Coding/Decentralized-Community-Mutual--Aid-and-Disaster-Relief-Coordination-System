package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.Donation;
import com.hnaungkyoe.service.DonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donations")
@CrossOrigin(origins = "*")
public class DonationController {
    private final DonationService service;

    @Autowired
    public DonationController(DonationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Donation> record(@RequestBody Donation donation) {
        return ResponseEntity.ok(service.recordDonation(donation));
    }

    @GetMapping
    public ResponseEntity<List<Donation>> getAll() {
        return ResponseEntity.ok(service.getAllDonations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Donation> getById(@PathVariable Long id) {
        return service.getDonationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}