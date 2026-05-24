package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.Donation;
import com.hnaungkyoe.entity.User;
import com.hnaungkyoe.service.DonationService;
import com.hnaungkyoe.repository.UserRepository; // 💡 UserRepository ကို သေချာ သွင်းထားပါတယ်
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/donations")
@CrossOrigin(origins = "*")
public class DonationController {

    @Autowired private DonationService service;
    @Autowired private UserRepository userRepository; // 💡 အလှူရှင်ကို ရှာဖွေရန် UserRepository ကို တွဲဖက်ခေါ်ယူခြင်း ✨

    @PostMapping
    public ResponseEntity<?> record(@RequestBody Donation donation) {
        // Frontend ကနေ ပါလာတဲ့ Donor Object (ID) ကို သုံးပြီး တိုက်ရိုက် သိမ်းဆည်းလိုက်ခြင်း 🚀
        // ဒါဆိုရင် Spring Security ရဲ့ အမည်ကွဲလွဲမှုတွေကြောင့် NULL ဖြစ်ရတဲ့ ပြဿနာ လုံးဝ(လုံးဝ) ပြီးဆုံးသွားပါပြီ။
        return ResponseEntity.ok(service.recordDonation(donation));
    }
    @GetMapping
    public ResponseEntity<List<Donation>> getAll() {
        return ResponseEntity.ok(service.getAllDonations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return service.getDonationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Donation>> getPending() {
        return ResponseEntity.ok(service.getPendingDonations());
    }

    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<List<Donation>> getByCampaign(@PathVariable Long campaignId) {
        return ResponseEntity.ok(service.getByCampaignId(campaignId));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.confirmDonation(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.rejectDonation(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ✨ အသစ်တိုးလိုက်သော Endpoint: လက်ရှိ Login ဝင်ထားသူရဲ့ Donation History သီးသန့်ဆွဲထုတ်ခြင်း ✨
    @GetMapping("/my")
    public ResponseEntity<List<Donation>> getMyDonations() {
        return ResponseEntity.ok(service.getMyDonationsHistory());
    }
}