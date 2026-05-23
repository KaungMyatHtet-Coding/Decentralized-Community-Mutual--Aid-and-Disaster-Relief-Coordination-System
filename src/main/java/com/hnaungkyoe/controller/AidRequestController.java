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

    @Autowired private AidRequestService service;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody AidRequest request) {
        try {
            return ResponseEntity.ok(service.createAidRequest(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<AidRequest>> getAll() {
        return ResponseEntity.ok(service.getAllAidRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return service.getAidRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AidRequest>> getByStatus(@PathVariable AidRequest.Status status) {
        return ResponseEntity.ok(service.getByStatus(status));
    }

    @GetMapping("/township/{township}")
    public ResponseEntity<List<AidRequest>> getByTownship(@PathVariable String township) {
        return ResponseEntity.ok(service.getByTownship(township));
    }

    // Status update endpoint
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestParam AidRequest.Status status,
                                          @RequestParam Long adminId) {
        try {
            return ResponseEntity.ok(service.updateStatus(id, status, adminId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.deleteAidRequest(id);
        return ResponseEntity.ok("Aid request deleted successfully");
    }
}