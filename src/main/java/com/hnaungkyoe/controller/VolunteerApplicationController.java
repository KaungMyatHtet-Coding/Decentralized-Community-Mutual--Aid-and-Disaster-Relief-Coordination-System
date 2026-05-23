package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.VolunteerApplication;
import com.hnaungkyoe.service.VolunteerApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/volunteer-applications")
@CrossOrigin(origins = "*")
public class VolunteerApplicationController {
    private final VolunteerApplicationService service;

    @Autowired
    public VolunteerApplicationController(VolunteerApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<VolunteerApplication> apply(@RequestBody VolunteerApplication application) {
        return ResponseEntity.ok(service.apply(application));
    }

    @GetMapping
    public ResponseEntity<List<VolunteerApplication>> getAll() {
        return ResponseEntity.ok(service.getAllApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VolunteerApplication> getById(@PathVariable Long id) {
        return service.getApplicationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}