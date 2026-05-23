package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.Notification;
import com.hnaungkyoe.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    private final NotificationService service;

    @Autowired
    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Notification> send(@RequestBody Notification notification) {
        return ResponseEntity.ok(service.sendNotification(notification));
    }

    @GetMapping("/unread/{userId}")
    public ResponseEntity<List<Notification>> getUnread(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getUnreadNotificationsForUser(userId));
    }
}