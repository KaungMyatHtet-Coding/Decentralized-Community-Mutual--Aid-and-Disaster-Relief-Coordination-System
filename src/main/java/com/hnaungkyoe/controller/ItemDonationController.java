package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.*;
import com.hnaungkyoe.repository.ItemDonationRepository;
import com.hnaungkyoe.service.ItemDonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/item-donations")
@CrossOrigin(origins = "*")
public class ItemDonationController {

    @Autowired
    private ItemDonationService service;
    @Autowired
    private ItemDonationService itemDonationService;

    // 💡 Admin ရဲ့ Review Queue (Pending) အတွက် သီးသန့် Path
    @GetMapping("/pending")
    public ResponseEntity<List<ItemDonation>> getAdminPendingDonations() {
        return ResponseEntity.ok(service.getPendingItemDonations());
    }

    @PostMapping
    public ResponseEntity<?> submit(
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal User currentUser) {
        try {
            return ResponseEntity.ok(service.submitItemDonation(payload, currentUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approveAndAssign(@PathVariable Long id, @RequestParam Long volunteerId) {
        try {
            return ResponseEntity.ok(service.approveAndAssign(id, volunteerId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/admin-reject")
    public ResponseEntity<?> adminReject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String reason = body.getOrDefault("reason", "");
            return ResponseEntity.ok(service.rejectItemDonation(id, reason));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/mark-as-stored")
    public ResponseEntity<?> markAsStored(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.markAsStored(id, null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ItemDonation>> getAll() {
        try {
            return ResponseEntity.ok(service.getAll());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<ItemDonation>> getMyDonations(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.getMyItemDonations(currentUser.getId()));
    }

    @GetMapping("/assigned")
    public ResponseEntity<List<ItemDonation>> getAssigned(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false, defaultValue = "active") String type) {
        try {
            return ResponseEntity.ok(service.getVolunteerAssigned(currentUser.getId(), type));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<?> acceptAssignment(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        try {
            return ResponseEntity.ok(service.volunteerAccept(id, currentUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> rejectAssignment(@PathVariable Long id, @RequestBody Map<String, String> body, @AuthenticationPrincipal User currentUser) {
        try {
            String reason = body.getOrDefault("reason", "No reason provided");
            return ResponseEntity.ok(service.volunteerReject(id, reason, currentUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/volunteer-confirm")
    public ResponseEntity<?> volunteerConfirm(@PathVariable Long id, @RequestBody Map<String, String> body, @AuthenticationPrincipal User currentUser) {
        try {
            return ResponseEntity.ok(service.volunteerConfirm(id, body.get("confirmPhotoUrl"), body.get("note"), currentUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/approve-auto")
    public ResponseEntity<?> approveAndAutoAssign(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.autoAssignNearestVolunteer(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/generate-otp")
    public ResponseEntity<?> generateOtp(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        try {
            String otp = service.generateHandoverOtp(id, currentUser);
            return ResponseEntity.ok(Map.of("message", "OTP generated", "otp", otp));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/verify-otp")
    public ResponseEntity<?> verifyOtp(@PathVariable Long id, @RequestBody Map<String, String> body, @AuthenticationPrincipal User currentUser) {
        try {
            String otp = body.get("otp");
            return ResponseEntity.ok(service.verifyOtpAndConfirm(id, otp, currentUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/confirm-photo")
    public ResponseEntity<?> confirmWithPhoto(@PathVariable Long id, @RequestBody Map<String, String> body, @AuthenticationPrincipal User currentUser) {
        try {
            return ResponseEntity.ok(service.confirmWithPhoto(id, body.get("photoUrl"), body.get("note"), currentUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/final-store")
    public ResponseEntity<?> finalStore(@PathVariable Long id, @RequestBody Map<String, String> body, @AuthenticationPrincipal User currentUser) {
        try {
            String notes = body.get("notes");
            return ResponseEntity.ok(service.finalStoreConfirmation(id, currentUser, notes));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // ညီလေးရဲ့ ItemDonationController.java ထဲတွင် တိုးထည့်ရန်

    // ညီလေးရဲ့ ItemDonationController.java ထဲက အစ်ကိုတို့ စောစောက ရေးခဲ့တဲ့ @GetMapping ကို အခုလို အချောသတ် ပြောင်းပေးပါ

    @Autowired private ItemDonationRepository itemDonationRepository; // Repository ကို သုံးဖို့ တိုက်ရိုက် ခေါ်ပါမယ်

    @GetMapping("/admin/pending-store")
    public ResponseEntity<List<ItemDonation>> getPendingStoreDonations() {
        // 💡 Status က VOLUNTEER_RECEIVED ဖြစ်ပြီး ဂိုဒေါင်မသွင်းရသေးတဲ့ ပစ္စည်းတွေကို Repository ကနေ တိုက်ရိုက်အပိုင် ဆွဲထုတ်ခြင်း
        List<ItemDonation> pendingItems = itemDonationRepository.findByStatus(ItemDonation.Status.VOLUNTEER_RECEIVED);
        return ResponseEntity.ok(pendingItems);
    }
    // ✅ Admin က ပစ္စည်းကို စစ်ဆေးပြီး ဂိုဒေါင်ထဲ သွင်းလိုက်သည့် API (Approve & Store)
    @PostMapping("/{id}/approve-and-store")
    public ResponseEntity<?> approveAndStore(
            @PathVariable Long id,
            @RequestParam String remarks,
            @AuthenticationPrincipal User admin) {
        try {
            // 💡 စာလုံးအသေး 'itemDonationService' ကို သေချာပေါက် သုံးပေးရပါမယ်ဗျာ
            return ResponseEntity.ok(itemDonationService.approveAndStoreInInventory(id, remarks, admin));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}