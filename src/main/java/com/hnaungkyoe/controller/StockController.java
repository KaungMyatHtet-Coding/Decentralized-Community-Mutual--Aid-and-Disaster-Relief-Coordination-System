package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.Stock;
import com.hnaungkyoe.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hnaungkyoe.dto.EmergencyUsageRequest;
import com.hnaungkyoe.entity.EmergencyUsageLog;
import com.hnaungkyoe.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stocks")
@CrossOrigin(origins = "*")
public class StockController {
    private final StockService service;

    @Autowired
    public StockController(StockService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Stock> addOrUpdate(@RequestBody Stock stock) {
        return ResponseEntity.ok(service.addOrUpdateStock(stock));
    }

    @PostMapping("/emergency-usage")
    public ResponseEntity<?> recordEmergencyUsage(
            @RequestBody EmergencyUsageRequest request,
            @AuthenticationPrincipal User currentUser) {
        try {
            String authorizedBy = currentUser != null ? currentUser.getUsername() : "System";
            Stock updated = service.recordEmergencyUsage(request, authorizedBy);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/emergency-logs")
    public ResponseEntity<List<EmergencyUsageLog>> getEmergencyLogs(
            @RequestParam(required = false) String township,
            @AuthenticationPrincipal User currentUser) {
        
        // If it's a sub-admin, force filter by their township
        if (currentUser != null && currentUser.getRole() == User.Role.ROLE_SUB_ADMIN) {
            return ResponseEntity.ok(service.getEmergencyLogs(currentUser.getTownship()));
        }
        
        // Otherwise use the requested township (or all if null)
        return ResponseEntity.ok(service.getEmergencyLogs(township));
    }

    @GetMapping
    public ResponseEntity<List<Stock>> getAll(@RequestParam(required = false) String township) {
        if (township != null && !township.isEmpty()) {
            return ResponseEntity.ok(service.getStocksByTownship(township));
        }
        return ResponseEntity.ok(service.getAllStocks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Stock> getById(@PathVariable Long id) {
        return service.getStockById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}