package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.Notification;
import com.hnaungkyoe.entity.User;
import com.hnaungkyoe.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications") // 💡 api.js ရဲ့ baseURL (/api) နဲ့ ကွက်တိဖြစ်အောင် ပြန်ညှိထားပါတယ်
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired private NotificationService service;

    // ✅ သက်ဆိုင်ရာ User ရဲ့ Notification အားလုံး ဆွဲထုတ်ခြင်း
    @GetMapping("/my")
    public ResponseEntity<List<Notification>> getMyNotifications(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.getAllNotificationsForUser(currentUser.getId()));
    }

    // ✅ Notification တစ်ခုချင်းစီကို Read အဖြစ် မှတ်သားခြင်း (PUT Method သို့ ပြောင်းလဲထားပါသည်)
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markOneAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        try {
            return ResponseEntity.ok(service.markOneAsRead(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ✅ Notification အားလုံးကိုတစ်ပြိုင်နက် Read အဖြစ် မှတ်သားခြင်း (PUT Method သို့ ပြောင်းလဲထားပါသည်)
    @PutMapping("/mark-all-read")
    public ResponseEntity<?> markAllRead(
            @AuthenticationPrincipal User currentUser) {
        service.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok("All notifications marked as read");
    }

    // 🚨 Trigger SOS Alert
    @PostMapping("/sos")
    public ResponseEntity<?> triggerSOS(
            @RequestBody java.util.Map<String, String> payload,
            @AuthenticationPrincipal User currentUser) {
        try {
            String message = payload.get("message");
            String township = currentUser.getTownship();
            if (township == null || township.isEmpty()) {
                return ResponseEntity.badRequest().body("Your profile does not have a township set. Please update your profile before sending an SOS.");
            }
            if (message == null || message.trim().isEmpty()) {
                message = "Emergency Assistance Needed!";
            }
            
            service.sendSOSToTownship(township, message, currentUser);
            return ResponseEntity.ok(java.util.Map.of("success", true, "message", "SOS Alert broadcasted successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}