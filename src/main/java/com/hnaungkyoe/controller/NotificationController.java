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

    @Autowired private NotificationService service;

    @GetMapping("/unread/{userId}")
    public ResponseEntity<List<Notification>> getUnread(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getUnreadNotificationsForUser(userId));
    }

    @GetMapping("/all/{userId}")
    public ResponseEntity<List<Notification>> getAll(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getAllNotificationsForUser(userId));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markOneAsRead(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.markOneAsRead(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/read-all/{userId}")
    public ResponseEntity<?> markAllAsRead(@PathVariable Long userId) {
        service.markAllAsRead(userId);
        return ResponseEntity.ok("All notifications marked as read");
    }
}