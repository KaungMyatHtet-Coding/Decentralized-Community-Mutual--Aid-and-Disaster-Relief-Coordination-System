package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.Notification;
import com.hnaungkyoe.entity.User;
import com.hnaungkyoe.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired private NotificationService service;

    // ✅ JWT ကနေ user ယူ — /my
    @GetMapping("/my")
    public ResponseEntity<List<Notification>> getMyNotifications(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                service.getAllNotificationsForUser(currentUser.getId())
        );
    }

    // ✅ Unread သာ ကြည့်ချင်ရင်
    @GetMapping("/my/unread")
    public ResponseEntity<List<Notification>> getMyUnread(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                service.getUnreadNotificationsForUser(currentUser.getId())
        );
    }

    // ✅ တစ်ခုချင်း read mark
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markOneAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        try {
            return ResponseEntity.ok(service.markOneAsRead(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ✅ အားလုံး read mark
    @Transactional
    @PatchMapping("/mark-all-read")
    public ResponseEntity<?> markAllRead(
            @AuthenticationPrincipal User currentUser) {
        service.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok("All notifications marked as read");
    }

    // ✅ Old endpoints — backward compatible (မဖျက်ဘဲ ထားတယ်)
    @GetMapping("/unread/{userId}")
    public ResponseEntity<List<Notification>> getUnread(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                service.getUnreadNotificationsForUser(userId)
        );
    }

    @GetMapping("/all/{userId}")
    public ResponseEntity<List<Notification>> getAll(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                service.getAllNotificationsForUser(userId)
        );
    }

    @PatchMapping("/read-all/{userId}")
    public ResponseEntity<?> markAllAsRead(@PathVariable Long userId) {
        service.markAllAsRead(userId);
        return ResponseEntity.ok("All notifications marked as read");
    }
}