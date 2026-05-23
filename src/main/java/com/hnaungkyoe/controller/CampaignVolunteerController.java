package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.CampaignVolunteer;
import com.hnaungkyoe.service.CampaignVolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaign-volunteers")
@CrossOrigin(origins = "*")
public class CampaignVolunteerController {
    private final CampaignVolunteerService service;

    @Autowired
    public CampaignVolunteerController(CampaignVolunteerService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CampaignVolunteer> assign(@RequestBody CampaignVolunteer cv) {
        return ResponseEntity.ok(service.assignVolunteer(cv));
    }

    @GetMapping
    public ResponseEntity<List<CampaignVolunteer>> getAll() {
        return ResponseEntity.ok(service.getAllAssignments());
    }
}