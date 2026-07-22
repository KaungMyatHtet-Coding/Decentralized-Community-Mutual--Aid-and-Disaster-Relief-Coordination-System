package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.Campaign;
import com.hnaungkyoe.service.CampaignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.hnaungkyoe.entity.User;

@RestController
@RequestMapping("/api/campaigns")
@CrossOrigin(origins = "*")
public class CampaignController {
    private final CampaignService service;

    @Autowired
    public CampaignController(CampaignService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Campaign> create(@RequestBody Campaign campaign, @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.createCampaign(campaign, currentUser));
    }

    @GetMapping
    public ResponseEntity<List<Campaign>> getAll() {
        return ResponseEntity.ok(service.getAllCampaigns());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Campaign> getById(@PathVariable Long id) {
        return service.getCampaignById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}")
    public ResponseEntity<Campaign> update(@PathVariable Long id, @RequestBody Campaign campaign, @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.updateCampaign(id, campaign, currentUser));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Campaign> approve(@PathVariable Long id) {
        return ResponseEntity.ok(service.approveCampaign(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Campaign> reject(@PathVariable Long id) {
        return ResponseEntity.ok(service.rejectCampaign(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.deleteCampaign(id);
        return ResponseEntity.ok("Campaign deleted.");
    }
}