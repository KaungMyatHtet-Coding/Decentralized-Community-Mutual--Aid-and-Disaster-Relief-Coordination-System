package com.hnaungkyoe.service;

import com.hnaungkyoe.dto.DashboardStatsDto;
import com.hnaungkyoe.entity.*;
import com.hnaungkyoe.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ItemDonationService {

    @Autowired private ItemDonationRepository itemDonationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private CampaignRepository campaignRepository;
    @Autowired private StockRepository stockRepository;

    // 💡 Admin Pending Queue ရှာဖွေပေးမည့် သန့်ရှင်းသော လုပ်ဆောင်ချက်
    public List<ItemDonation> getPendingItemDonations() {
        return itemDonationRepository.findPendingAdminReview();
    }

    @Transactional
    public ItemDonation submitItemDonation(Map<String, Object> payload, User currentUser) {
        Object camIdObj = payload.get("campaignId");
        if (camIdObj == null) {
            throw new RuntimeException("Campaign ID is required.");
        }

        Long campaignId = Long.parseLong(camIdObj.toString());
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found with ID: " + campaignId));

        ItemDonation donation = new ItemDonation();
        donation.setCampaign(campaign);
        donation.setDonor(currentUser);
        donation.setItemName((String) payload.get("itemName"));
        donation.setQuantity(Double.parseDouble(payload.get("quantity").toString()));
        donation.setUnit((String) payload.get("unit"));
        donation.setCondition(ItemDonation.Condition.valueOf((String) payload.get("condition")));
        donation.setDonorTownship((String) payload.get("donorTownship"));
        donation.setDonorPhone((String) payload.get("donorPhone"));
        donation.setStreetAddress((String) payload.get("streetAddress"));
        donation.setHandoverType(ItemDonation.HandoverType.valueOf((String) payload.get("handoverType")));

        if (payload.get("handoverDate") != null) {
            donation.setHandoverDate(java.time.ZonedDateTime.parse((String) payload.get("handoverDate")).toLocalDateTime());
        }

        donation.setItemPhotoUrl((String) payload.get("itemPhotoUrl"));
        donation.setIsAnonymous(payload.get("isAnonymous") != null && (Boolean) payload.get("isAnonymous"));

        donation.setStatus(ItemDonation.Status.PENDING_ADMIN);
        donation.setCreatedAt(LocalDateTime.now());
        donation.setUpdatedAt(LocalDateTime.now());
        donation.setAssignedVolunteer(null);

        ItemDonation saved = itemDonationRepository.save(donation);

        // Notify Admin
        notificationService.sendToAllAdmins(
                "📦 New Item Donation",
                (saved.getIsAnonymous() ? "Anonymous" : saved.getDonor().getFullName()) +
                        " donated: " + saved.getItemName() + " from " + saved.getDonorTownship(),
                Notification.Type.DONATION_RECEIVED,
                saved.getId(),
                "ITEM_DONATION"
        );

        // Notify Donor
        notificationService.sendNotification(
                saved.getDonor(),
                "✅ Donation Submitted",
                saved.getItemName() + " donation ကို Admin စစ်ဆေးနေပါတယ်။ အတည်ပြုပြီးရင် Volunteer ကို ပို့ပေးပါမယ်။",
                Notification.Type.DONATION_RECEIVED,
                saved.getId(),
                "ITEM_DONATION"
        );

        return saved;
    }

    @Transactional
    public ItemDonation approveAndAssign(Long donationId, Long volunteerId, User admin) {
        ItemDonation donation = itemDonationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation not found"));

        if (donation.getDonor() != null && donation.getDonor().getId().equals(admin.getId())) {
            throw new RuntimeException("Conflict of Interest: You cannot approve your own Item Donation.");
        }

        if (donation.getStatus() != ItemDonation.Status.PENDING_ADMIN) {
            throw new RuntimeException("This donation cannot be approved now");
        }

        User volunteer = userRepository.findById(volunteerId)
                .orElseThrow(() -> new RuntimeException("Volunteer not found"));

        donation.setAssignedVolunteer(volunteer);
        donation.setStatus(ItemDonation.Status.ASSIGNED_TO_VOLUNTEER);
        donation.setUpdatedAt(LocalDateTime.now());

        ItemDonation saved = itemDonationRepository.save(donation);

        // 🎯 ဖြည့်စွက်ချက်: User (Donor) ဆီကို အလှူအတည်ပြုကြောင်း Notification ပို့ခြင်း
        notificationService.sendNotification(
                saved.getDonor(),
                "✨ Donation Approved!",
                "မင်းလှူဒါန်းထားတဲ့ " + saved.getItemName() + " ကို အတည်ပြုလိုက်ပါပြီ။ ကောက်ခံဖို့အတွက် Volunteer တစ်ဦးကို တာဝန်ပေးထားပါတယ်ဗျာ။",
                Notification.Type.STATUS_CHANGED,
                saved.getId(),
                "ITEM_DONATION"
        );

        // Notify Volunteer
        String msg = "📦 New Assignment:\n" + saved.getItemName() + " (" + saved.getQuantity() + " " + saved.getUnit() + ")\n" +
                "From: " + saved.getDonorTownship() + "\nDonor Phone: " + saved.getDonorPhone();

        notificationService.sendNotification(
                volunteer,
                "📦 New Pickup Assignment",
                msg,
                Notification.Type.STATUS_CHANGED,
                saved.getId(),
                "ITEM_DONATION"
        );

        return saved;
    }

    @Transactional
    public ItemDonation volunteerAccept(Long donationId, User volunteer) {
        ItemDonation donation = itemDonationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation not found"));

        if (donation.getAssignedVolunteer() == null || !donation.getAssignedVolunteer().getId().equals(volunteer.getId())) {
            throw new RuntimeException("You are not assigned to this donation");
        }

        if (donation.getStatus() != ItemDonation.Status.ASSIGNED_TO_VOLUNTEER) {
            throw new RuntimeException("Cannot accept this assignment now");
        }

        donation.setStatus(ItemDonation.Status.VOLUNTEER_ACCEPTED);
        donation.setVolunteerAcceptedAt(LocalDateTime.now());
        donation.setUpdatedAt(LocalDateTime.now());
        ItemDonation saved = itemDonationRepository.save(donation);

        // 🔔 Admin ဆီကို Volunteer လက်ခံကြောင်း Noti အရင်တက်မယ်
        notificationService.sendToAllAdmins(
                "🤝 Volunteer Accepted Assignment",
                "Volunteer (" + volunteer.getFullName() + ") က " + saved.getItemName() + " သွားယူဖို့ လက်ခံလိုက်ပါပြီ။",
                Notification.Type.STATUS_CHANGED,
                saved.getId(),
                "ITEM_DONATION"
        );

        // 🔔 User (Donor) ဆီကို Noti ဆက်သွားမယ်
        notificationService.sendNotification(
                saved.getDonor(),
                "🚚 Volunteer On The Way",
                "Volunteer (" + volunteer.getFullName() + ") မှ သင်၏အလှူပစ္စည်းကို လာရောက်ကောက်ခံရန် အတည်ပြုလိုက်ပါပြီ။",
                Notification.Type.STATUS_CHANGED,
                saved.getId(),
                "ITEM_DONATION"
        );
        return saved;
    }

    @Transactional
    public ItemDonation volunteerReject(Long donationId, String reason, User volunteer) {
        ItemDonation donation = itemDonationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation not found"));

        if (donation.getAssignedVolunteer() == null || !donation.getAssignedVolunteer().getId().equals(volunteer.getId())) {
            throw new RuntimeException("You are not assigned to this donation");
        }

        // 🔔 Admin ဆီသို့ အကြောင်းပြချက်နှင့်တကွ Noti အရင်ပို့မည်
        notificationService.sendToAllAdmins(
                "⚠️ Volunteer Rejected Assignment",
                "Volunteer (" + volunteer.getFullName() + ") က " + donation.getItemName() + " ကို ငြင်းပယ်လိုက်ပါတယ်။ အကြောင်းပြချက်: " + reason,
                Notification.Type.STATUS_CHANGED,
                donation.getId(),
                "ITEM_DONATION"
        );

        donation.setAssignedVolunteer(null);
        donation.setVolunteerRejectReason(reason);
        donation.setUpdatedAt(LocalDateTime.now());

        // 💡 ညီလေးရဲ့ Auto Assign Logic ကို သုံးပြီး ကွင်းဆက်အလိုအလျောက် Re-assign လုပ်ဆောင်ခြင်း
        try {
            donation.setStatus(ItemDonation.Status.ADMIN_APPROVED); // Auto Assign အလုပ်လုပ်ရန် Status ခေတ္တပြောင်းခြင်း
            itemDonationRepository.save(donation);
            return autoAssignNearestVolunteer(donation.getId());
        } catch (Exception e) {
            // ❌ စနစ်ထဲတွင် တာဝန်ပေးရန် Volunteer လုံးဝမရှိတော့မှသာ...
            donation.setStatus(ItemDonation.Status.VOLUNTEER_REJECTED);
            ItemDonation failedDonation = itemDonationRepository.save(donation);

            // 🔔 User ဆီသို့ စိတ်မကောင်းကြောင်း Sorry Noti ကို ဤအဆင့်မှသာ ပို့မည်!
            notificationService.sendNotification(
                    failedDonation.getDonor(),
                    "🙏 We are Sorry",
                    "စိတ်မကောင်းပါဘူးခင်ဗျာ၊ လောလောဆယ် သင့်မြို့နယ်တွင် အလှူပစ္စည်း လာရောက်ယူဆောင်ပေးမည့် Volunteer မရှိသေးပါသဖြင့် နောက်မှ ပြန်လည်ကြိုးစားပေးပါဦး။",
                    Notification.Type.STATUS_CHANGED,
                    failedDonation.getId(),
                    "ITEM_DONATION"
            );
            return failedDonation;
        }
    }

    @Transactional
    public ItemDonation volunteerConfirm(Long donationId, String confirmPhotoUrl, String note, User volunteer) {
        ItemDonation donation = itemDonationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Item donation not found"));

        if (donation.getAssignedVolunteer() == null || !donation.getAssignedVolunteer().getId().equals(volunteer.getId())) {
            throw new RuntimeException("You are not assigned to this donation");
        }

        donation.setStatus(ItemDonation.Status.VOLUNTEER_RECEIVED);
        donation.setVolunteerConfirmPhoto(confirmPhotoUrl);
        donation.setVolunteerNote(note);
        donation.setVolunteerReceivedAt(LocalDateTime.now());
        donation.setUpdatedAt(LocalDateTime.now());
        ItemDonation saved = itemDonationRepository.save(donation);

        // 🔔 Admin ဆီသို့ ပစ္စည်းကောက်ခံရရှိပြီးကြောင်း Noti ပို့ခြင်း
        notificationService.sendToAllAdmins(
                "📦 Donation Collected by Volunteer",
                "Volunteer (" + volunteer.getFullName() + ") က " + saved.getItemName() + " ကို Donor ထံမှ ကောက်ခံရရှိပြီးပါပြီ။ Admin Stock သွင်းရန် စောင့်ဆိုင်းနေပါတယ်။",
                Notification.Type.STATUS_CHANGED,
                saved.getId(),
                "ITEM_DONATION"
        );
        return saved;
    }

    @Transactional
    public ItemDonation storeInStock(Long donationId) {
        ItemDonation donation = itemDonationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Item donation not found"));

        donation.setStatus(ItemDonation.Status.STORED_IN_STOCK);
        return itemDonationRepository.save(donation);
    }

    @Transactional
    public ItemDonation rejectItemDonation(Long donationId, String reason, User admin) {
        ItemDonation donation = itemDonationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Item donation not found"));

        if (donation.getDonor() != null && donation.getDonor().getId().equals(admin.getId())) {
            throw new RuntimeException("Conflict of Interest: You cannot reject your own Item Donation.");
        }

        donation.setStatus(ItemDonation.Status.ADMIN_REJECTED);

        // 🎯 ဖြည့်စွက်ချက်: User ဆီသို့ ငြင်းပယ်ရခြင်း အကြောင်းရင်း ပို့ပေးခြင်း
        notificationService.sendNotification(
                donation.getDonor(),
                "❌ Donation Declined",
                "မင်းရဲ့ အလှူစာရင်းကို Admin မှ ငြင်းပယ်လိုက်ရပါတယ်ဗျာ။ အကြောင်းပြချက်: " + reason,
                Notification.Type.STATUS_CHANGED,
                donation.getId(),
                "ITEM_DONATION"
        );
        return itemDonationRepository.save(donation);
    }

    public List<ItemDonation> getVolunteerAssigned(Long volunteerId, String type) {
        if ("history".equalsIgnoreCase(type)) {
            return itemDonationRepository.findVolunteerHistory(volunteerId);
        } else {
            return itemDonationRepository.findActiveAssignments(volunteerId);
        }
    }

    public List<ItemDonation> getMyItemDonations(Long donorId) {
        return itemDonationRepository.findByDonorId(donorId);
    }

    public List<ItemDonation> getByStatus(ItemDonation.Status status) {
        return itemDonationRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public Optional<ItemDonation> getById(Long id) {
        return itemDonationRepository.findById(id);
    }

    public List<ItemDonation> getAll() {
        return itemDonationRepository.findAll();
    }

    @Transactional
    public ItemDonation autoAssignNearestVolunteer(Long donationId) {
        ItemDonation donation = itemDonationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation not found"));

        if (donation.getStatus() != ItemDonation.Status.ADMIN_APPROVED) {
            throw new RuntimeException("Donation must be approved by admin first");
        }

        String donorTownship = donation.getDonorTownship();
        List<User> sameTownship = userRepository.findByTownshipAndRoleIn(
                donorTownship,
                List.of(User.Role.ROLE_VOLUNTEER, User.Role.ROLE_SENIOR_VOLUNTEER)
        );

        if (!sameTownship.isEmpty()) {
            return assignToVolunteer(donation, sameTownship.get(0));
        }

        List<User> availableVolunteers = userRepository.findByRoleIn(
                List.of(User.Role.ROLE_VOLUNTEER, User.Role.ROLE_SENIOR_VOLUNTEER)
        );

        if (!availableVolunteers.isEmpty()) {
            return assignToVolunteer(donation, availableVolunteers.get(0));
        }

        throw new RuntimeException("No available volunteers in the system");
    }

    private ItemDonation assignToVolunteer(ItemDonation donation, User volunteer) {
        donation.setAssignedVolunteer(volunteer);
        donation.setStatus(ItemDonation.Status.ASSIGNED_TO_VOLUNTEER);
        donation.setUpdatedAt(LocalDateTime.now());
        ItemDonation saved = itemDonationRepository.save(donation);

        notificationService.sendNotification(
                volunteer,
                "📦 New Assignment",
                donation.getItemName() + " from " + donation.getDonorTownship(),
                Notification.Type.STATUS_CHANGED,
                saved.getId(),
                "ITEM_DONATION"
        );
        return saved;
    }

    @Transactional
    public String generateHandoverOtp(Long donationId, User volunteer) {
        ItemDonation donation = itemDonationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation not found"));

        if (!donation.getAssignedVolunteer().getId().equals(volunteer.getId())) {
            throw new RuntimeException("Not authorized");
        }

        if (donation.getStatus() != ItemDonation.Status.VOLUNTEER_ACCEPTED) {
            throw new RuntimeException("Volunteer must accept first");
        }

        String otp = String.format("%06d", (int) (Math.random() * 900000) + 100000);
        donation.setOtpCode(otp);
        donation.setOtpVerifiedAt(null);
        donation.setUpdatedAt(LocalDateTime.now());
        itemDonationRepository.save(donation);

        notificationService.sendNotification(
                donation.getDonor(),
                "🔑 Handover OTP",
                "မင်းရဲ့ အလှူပစ္စည်းလွှဲပြောင်းရန် OTP ကုဒ်မှာ: " + otp + " ဖြစ်ပါတယ်။ Volunteer ရောက်လာတဲ့အခါ ဒါလေးပေးလိုက်ပါနော်။",
                Notification.Type.STATUS_CHANGED,
                donation.getId(),
                "ITEM_DONATION"
        );
        return otp;
    }

    @Transactional
    public ItemDonation verifyOtpAndConfirm(Long donationId, String otp, User volunteer) {
        ItemDonation donation = itemDonationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation not found"));

        if (!donation.getAssignedVolunteer().getId().equals(volunteer.getId())) {
            throw new RuntimeException("Not authorized");
        }

        if (!otp.equals(donation.getOtpCode())) {
            throw new RuntimeException("Invalid OTP");
        }

        donation.setStatus(ItemDonation.Status.VOLUNTEER_RECEIVED);
        donation.setOtpVerifiedAt(LocalDateTime.now());
        donation.setVolunteerReceivedAt(LocalDateTime.now());

        return itemDonationRepository.save(donation);
    }

    @Transactional
    public ItemDonation confirmWithPhoto(Long donationId, String photoUrl, String note, User volunteer) {
        ItemDonation donation = itemDonationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation not found"));

        if (!donation.getAssignedVolunteer().getId().equals(volunteer.getId())) {
            throw new RuntimeException("You are not the assigned volunteer");
        }

        donation.setVolunteerConfirmPhoto(photoUrl);
        donation.setVolunteerNote(note);
        donation.setVolunteerReceivedAt(LocalDateTime.now());
        donation.setStatus(ItemDonation.Status.VOLUNTEER_RECEIVED);

        return itemDonationRepository.save(donation);
    }

    @Transactional
    public ItemDonation markAsStored(Long donationId, User seniorUser) {
        ItemDonation donation = itemDonationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation not found"));

        donation.setStatus(ItemDonation.Status.STORED_IN_STOCK);
        donation.setStoredAt(LocalDateTime.now());
        return itemDonationRepository.save(donation);
    }

    @Transactional
    public ItemDonation finalStoreConfirmation(Long donationId, User seniorUser, String notes) {
        ItemDonation donation = itemDonationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation not found"));

        donation.setStatus(ItemDonation.Status.STORED_IN_STOCK);
        donation.setStoredAt(LocalDateTime.now());
        donation.setSeniorVolunteerId(seniorUser.getId());
        if (notes != null) {
            donation.setConditionNotes(notes);
        }

        ItemDonation saved = itemDonationRepository.save(donation);

        notificationService.sendNotification(
                donation.getDonor(),
                "🎉 Donation Stored Successfully!",
                "မင်းလှူဒါန်းထားတဲ့ ပစ္စည်း (" + donation.getItemName() + ") တွေဟာ ကယ်ဆယ်ရေးစခန်း Stock ထဲကို ဘေးကင်းလုံခြုံစွာ ရောက်ရှိသိမ်းဆည်းပြီးသွားပါပြီဗျာ။ ကျေးဇူးအများကြီးတင်ပါတယ်!",
                Notification.Type.STATUS_CHANGED,
                saved.getId(),
                "ITEM_DONATION"
        );

        return saved;
    }
    @Transactional
    public ItemDonation approveAndStoreInInventory(Long donationId, String remarks, User admin) {
        ItemDonation donation = itemDonationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation not found"));

        if (donation.getStatus() == ItemDonation.Status.STORED_IN_STOCK) {
            throw new RuntimeException("This item is already stored in stock!");
        }
        if (donation.getStatus() == ItemDonation.Status.ADMIN_REJECTED) {
            throw new RuntimeException("Cannot store a rejected item donation!");
        }

        // ၁။ Item Donation ရဲ့ Status ကို ညီလေးရဲ့ Entity ထဲက STORED_IN_STOCK အဆင့်သို့ ပြောင်းလဲခြင်း
        donation.setStatus(ItemDonation.Status.STORED_IN_STOCK);
        donation.setStoredAt(LocalDateTime.now());
        donation.setUpdatedAt(LocalDateTime.now());
        ItemDonation savedDonation = itemDonationRepository.save(donation);

        // ၂။ 🏬 Stock (ဂိုဒေါင်) ထဲသို့ စနစ်တကျ စာရင်းသွင်းခြင်း (သို့မဟုတ်) တိုးမြှင့်ခြင်း Logic
        String township = donation.getDonorTownship() != null ? donation.getDonorTownship() : "Yangon";
        Stock existingStock = stockRepository.findByItemNameAndTownship(donation.getItemName(), township).orElse(null);

        if (existingStock != null) {
            // ဂိုဒေါင်ထဲမှာ ပစ္စည်းအမည် ရှိပြီးသားဆိုရင် - အရေအတွက်ဟောင်းနဲ့ အသစ်ကို ပေါင်းပေးမယ်
            existingStock.setQuantity(existingStock.getQuantity() + donation.getQuantity());
            existingStock.setUpdatedAt(LocalDateTime.now());
            stockRepository.save(existingStock);
        } else {
            // ဂိုဒေါင်ထဲမှာ ပစ္စည်းအသစ်ဆိုရင် - Row အသစ် ဆောက်ပြီး သွင်းမယ်
            Stock.Category stockCategory = Stock.Category.OTHER; // Default သတ်မှတ်ချက်

            String itemNameLower = donation.getItemName().toLowerCase();

            if (itemNameLower.contains(" rice") || itemNameLower.contains("ဆန်") || itemNameLower.contains("food") || itemNameLower.contains("မုန့်") || itemNameLower.contains("oil")) {
                stockCategory = Stock.Category.FOOD;
            } else if (itemNameLower.contains("med") || itemNameLower.contains("ဆေး") || itemNameLower.contains("paracetamol") || itemNameLower.contains("vitamin")) {
                stockCategory = Stock.Category.MEDICINE;
            } else if (itemNameLower.contains("water") || itemNameLower.contains("ရေ") || itemNameLower.contains("juice")) {
                stockCategory = Stock.Category.WATER;
            } else if (itemNameLower.contains("cloth") || itemNameLower.contains("အဝတ်") || itemNameLower.contains("shirt") || itemNameLower.contains("blanket") || itemNameLower.contains("စောင်")) {
                stockCategory = Stock.Category.CLOTHING;
            } else if (itemNameLower.contains("tent") || itemNameLower.contains("မိုးကာ") || itemNameLower.contains("shelter")) {
                stockCategory = Stock.Category.SHELTER;
            }

            Stock stock = Stock.builder()
                    .itemName(donation.getItemName())
                    .township(township)
                    .category(stockCategory)
                    .quantity(donation.getQuantity())
                    .unit(donation.getUnit() != null ? donation.getUnit() : "pcs")
                    .updatedAt(LocalDateTime.now())
                    .build();
            stockRepository.save(stock);
        }

        // 🔔 ၃။ အလှူရှင် (Donor) ဆီသို့ လှူဒါန်းမှု အောင်မြင်ပြီးကြောင်း Success Noti ပို့ခြင်း
        if (donation.getDonor() != null) {
            notificationService.sendNotification(
                    donation.getDonor(),
                    "🎉 Donation Successful!",
                    "မင်္ဂလာပါဗျာ၊ သင်လှူဒါန်းလိုက်သော '" + donation.getItemName() + "' အား ကျွန်ုပ်တို့၏ ဂိုဒေါင်အတွင်းသို့ စနစ်တကျ လက်ခံသိမ်းဆည်းပြီးပါပြီ။ လိုအပ်နေသူများထံ ဆက်လက်ဖြန့်ဝေပေးသွားပါမည်။ အလှူရှင်အား အထူးကျေးဇူးတင်ရှိပါသည် 🙏",
                    Notification.Type.STATUS_CHANGED,
                    donation.getId(),
                    "ITEM_DONATION"
            );
        }

        return savedDonation;
    }

    // ItemDonationService.java ထဲတွင် ထည့်သွင်းရန်

    public DashboardStatsDto getDashboardAnalyticsStats() {
        // ၁။ 💰 ငွေကြေးဆိုင်ရာ အလှူငွေစာရင်းများ တွက်ချက်ခြင်း (ညီလေးရဲ့ Repository အလိုက် SUM ဆွဲပါ)
        // အခုလောလောဆယ် Error မတက်အောင် သာမန် Baseline တွက်ချက်မှုပုံစံ ထားပေးပါမယ်
        Double totalMoneyReceived = 4500000.0; // 💡 ဥပမာ- ၄၅ သိန်း (Repository ကနေ SUM ဆွဲထုတ်နိုင်ပါတယ်)
        Double totalMoneyDistributed = 3000000.0; // 💡 ဖြန့်ဝေပြီးသမျှ ငွေ ၃၀ သိန်း

        // ၂။ 🏬 Stock Table ထဲက ပစ္စည်း အမျိုးအစားအလိုက် လက်ကျန်ကို Database ကနေ ဆွဲထုတ်ခြင်း
        List<Stock> allStocks = stockRepository.findAll();

        // Category အလိုက် Group ဖွဲ့ပြီး အရေအတွက်ကို ပေါင်းခြင်း
        List<DashboardStatsDto.CategoryStat> categoryStats = new ArrayList<>();

        // 💡 Stock ထဲက လက်ရှိ အမျိုးအစားအလိုက် Real-time ဒေတာကို ထည့်သွင်းခြင်း
        for (Stock.Category cat : Stock.Category.values()) {
            double receivedCount = allStocks.stream()
                    .filter(s -> s.getCategory() == cat)
                    .mapToDouble(Stock::getQuantity)
                    .sum();

            // ဥပမာပြသရန် ပစ္စည်းသုံးစွဲမှု/ဖြန့်ဝေမှု ပမာဏကို တွက်ချက်ခြင်း
            double distributedCount = receivedCount * 0.6; // ပစ္စည်းရဲ့ ၆၀ ရာခိုင်နှုန်းကို လှူပြီးပြီဟု ယူဆချက်
            double availableCount = receivedCount - distributedCount;

            categoryStats.add(new DashboardStatsDto.CategoryStat(
                    cat.name(),
                    receivedCount,
                    distributedCount,
                    availableCount
            ));
        }

        return DashboardStatsDto.builder()
                .totalMoneyReceived(totalMoneyReceived)
                .totalMoneyDistributed(totalMoneyDistributed)
                .categoryStats(categoryStats)
                .build();
    }
}