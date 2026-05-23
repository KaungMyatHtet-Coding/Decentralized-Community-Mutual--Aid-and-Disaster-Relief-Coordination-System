package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.Campaign;
import com.hnaungkyoe.service.CampaignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<Campaign> create(@RequestBody Campaign campaign) {
        return ResponseEntity.ok(service.createCampaign(campaign));
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
}