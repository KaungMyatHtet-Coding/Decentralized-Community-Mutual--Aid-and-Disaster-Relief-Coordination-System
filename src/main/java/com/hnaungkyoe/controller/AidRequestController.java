package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.AidRequest;
import com.hnaungkyoe.service.AidRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aid-requests")
@CrossOrigin(origins = "*")
public class AidRequestController {
    private final AidRequestService service;

    @Autowired
    public AidRequestController(AidRequestService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AidRequest> create(@RequestBody AidRequest request) {
        return ResponseEntity.ok(service.createAidRequest(request));
    }

    @GetMapping
    public ResponseEntity<List<AidRequest>> getAll() {
        return ResponseEntity.ok(service.getAllAidRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AidRequest> getById(@PathVariable Long id) {
        return service.getAidRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.deleteAidRequest(id);
        return ResponseEntity.ok("Aid request deleted successfully");
    }
}