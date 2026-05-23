package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.AuditLog;
import com.hnaungkyoe.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@CrossOrigin(origins = "*")
public class AuditLogController {
    private final AuditLogService service;

    @Autowired
    public AuditLogController(AuditLogService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AuditLog> log(@RequestBody AuditLog log) {
        return ResponseEntity.ok(service.logActivity(log));
    }

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAll() {
        return ResponseEntity.ok(service.getAllLogs());
    }
}