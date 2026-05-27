package com.hnaungkyoe.controller;

import com.hnaungkyoe.dto.DashboardStatsDto;
import com.hnaungkyoe.entity.*;
import com.hnaungkyoe.repository.DonationRepository;
import com.hnaungkyoe.repository.ItemDonationRepository;
import com.hnaungkyoe.service.ItemDonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
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
    @Autowired
    private DonationRepository donationRepository;

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
    // ItemDonationController.java ထဲတွင် ထည့်သွင်းရန်
// 📊 ၁။ Analytics Stats အတွက် Endpoint
    @GetMapping("/admin/analytics-stats")
    public ResponseEntity<DashboardStatsDto> getDashboardAnalytics() {
        return ResponseEntity.ok(itemDonationService.getDashboardAnalyticsStats());
    }

    @GetMapping("/all-history")
    public ResponseEntity<List<Map<String, Object>>> getAllDonationHistory() {
        List<Map<String, Object>> historyList = new ArrayList<>();

        // 📦 ၁။ အလှူပစ္စည်းစာရင်း (ITEM Donations ከ ItemDonation Table) များကို ဆွဲထုတ်ခြင်း
        List<ItemDonation> itemDonations = itemDonationRepository.findAll();
        for (ItemDonation idon : itemDonations) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", "ITEM_" + idon.getId());
            row.put("donorName", idon.getIsAnonymous() ? "Anonymous" : (idon.getDonor() != null ? idon.getDonor().getFullName() : "Anonymous"));
            row.put("type", "ITEM");
            row.put("item", idon.getItemName());
            row.put("amount", idon.getQuantity() + " " + idon.getUnit());
            row.put("date", idon.getCreatedAt() != null ? idon.getCreatedAt().toLocalDate().toString() : "-");
            row.put("status", idon.getStatus().name());
            historyList.add(row);
        }

        // 💰 ၂။ အလှူငွေစာရင်း (Donation Table) များကိုပါ ဆွဲထုတ်ပြီး ပေါင်းထည့်ခြင်း
        List<Donation> moneyDonations = donationRepository.findAll();
        for (Donation mdon : moneyDonations) {
            // 💡 ညီလေးရဲ့ Entity ထဲက Status Enum (CONFIRMED) အောင်မြင်ပြီးသားတွေကို စစ်ထုတ်ခြင်း
            if (Donation.Status.CONFIRMED == mdon.getStatus()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", "MONEY_" + mdon.getId());

                // 💡 Fix 1: getUser() အစား getDonor() ကို သုံးပြီး အလှူရှင်အမည် ယူခြင်း
                row.put("donorName", mdon.getDonor() != null ? mdon.getDonor().getFullName() : "Anonymous");

                // 💡 Fix 2: mdon.getDonationType().name() ကို သုံး၍ MONEY / ITEMS စာရင်းပြခြင်း
                row.put("type", mdon.getDonationType().name() == "ITEMS" ? "ITEM" : "MONEY");

                // Campaign ခေါင်းစဉ်ကို Details ထဲမှာ ပြပေးခြင်း
                row.put("item", mdon.getCampaign() != null ? mdon.getCampaign().getTitle() : "General Fund");

                // 💡 Fix 3: BigDecimal ကို Inconvertible types မဖြစ်အောင် .intValue() သို့မဟုတ် .doubleValue() ဖြင့် ပြောင်းလဲခြင်း
                int amountInt = mdon.getAmount() != null ? mdon.getAmount().intValue() : 0;
                row.put("amount", String.format("%,d", amountInt) + " MMK");

                row.put("date", mdon.getDonatedAt() != null ? mdon.getDonatedAt().toLocalDate().toString() : "-");
                row.put("status", "SUCCESS");

                historyList.add(row);
            }
        }

        // 📅 ၃။ ရက်စွဲအလိုက် အသစ်ဆုံးတွေကို ထိပ်ဆုံးကပြရန် Sort စီခြင်း
        historyList.sort((b, a) -> {
            String dateA = (String) a.get("date");
            String dateB = (String) b.get("date");
            if (dateA == null || "-".equals(dateA)) return 1;
            if (dateB == null || "-".equals(dateB)) return -1;
            return dateA.compareTo(dateB);
        });

        return ResponseEntity.ok(historyList);
    }
}