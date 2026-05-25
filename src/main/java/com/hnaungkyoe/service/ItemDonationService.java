package com.hnaungkyoe.service;

import com.hnaungkyoe.entity.*;
import com.hnaungkyoe.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ItemDonationService {

    @Autowired private ItemDonationRepository itemDonationRepository;
    @Autowired private VolunteerApplicationRepository volunteerApplicationRepository;
    @Autowired private StockRepository stockRepository;
    @Autowired private NotificationService notificationService;

    @Transactional
    public ItemDonation submitItemDonation(ItemDonation donation) {

        List<VolunteerApplication> volunteers = volunteerApplicationRepository
                .findByStatusAndOperatingTownship(
                        VolunteerApplication.Status.APPROVED,
                        donation.getDonorTownship()
                );

        // ✅ Volunteer assign ပဲ လုပ်၊ notify မလုပ်သေးဘူး
        if (!volunteers.isEmpty()) {
            User assignedVolunteer = volunteers.get(0).getUser();
            donation.setAssignedVolunteer(assignedVolunteer);
        }

        // ✅ အရင်ဆုံး save လုပ်လိုက် — ID ရပြီ
        ItemDonation saved = itemDonationRepository.save(donation);

        // ✅ Save ပြီးမှ volunteer notify — referenceId ပါပြီ
        if (saved.getAssignedVolunteer() != null) {
            String volunteerMsg;
            if (saved.getHandoverType() == ItemDonation.HandoverType.PICKUP) {
                volunteerMsg = "📍 PICKUP — " + saved.getDonorTownship() +
                        " မှ " + saved.getItemName() +
                        " (" + saved.getQuantity() + " " + saved.getUnit() + ")" +
                        " ကို " + saved.getDonor().getUsername() +
                        " ဆီ သွားယူပေးပါ။ 📞 " + saved.getDonorPhone() +
                        " | 📅 " + saved.getHandoverDate();
            } else {
                volunteerMsg = "📦 DELIVER — " + saved.getDonor().getUsername() +
                        " က " + saved.getItemName() +
                        " (" + saved.getQuantity() + " " + saved.getUnit() + ")" +
                        " ကို မင်းဆီ ပို့လာမယ်။ | 📅 " + saved.getHandoverDate();
            }

            notificationService.sendNotification(
                    saved.getAssignedVolunteer(),
                    "📦 Item Donation Assigned",
                    volunteerMsg,
                    Notification.Type.STATUS_CHANGED,
                    saved.getId(),   // ✅ null မဟုတ်တော့ဘူး
                    "ITEM_DONATION"
            );
        }

        // ✅ Admin notify — ဒါသာ မပါတာ ထည့်လိုက်
        notificationService.sendToAllAdmins(
                "📦 New Item Donation",
                (saved.getIsAnonymous() ? "Anonymous" : saved.getDonor().getUsername()) +
                        " donated: " + saved.getItemName() +
                        " (" + saved.getQuantity() + " " + saved.getUnit() + ")" +
                        " from " + saved.getDonorTownship(),
                Notification.Type.DONATION_RECEIVED,
                saved.getId(),
                "ITEM_DONATION"
        );

        // Donor notify
        notificationService.sendNotification(
                saved.getDonor(),
                "✅ Item Donation Submitted",
                saved.getItemName() + " donation တင်သွားပြီ။ Volunteer မကြာမီ ဆက်သွယ်ပါလိမ့်မယ်။",
                Notification.Type.DONATION_RECEIVED,
                saved.getId(),
                "ITEM_DONATION"
        );

        return saved;
    }

    // Volunteer — item လက်ခံပြီဆိုတာ confirm
    @Transactional
    public ItemDonation volunteerConfirm(Long donationId, String confirmPhotoUrl,
                                         String note, User volunteer) {
        ItemDonation donation = itemDonationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Item donation not found"));

        if (!donation.getAssignedVolunteer().getId().equals(volunteer.getId())) {
            throw new RuntimeException("You are not assigned to this donation");
        }

        donation.setStatus(ItemDonation.Status.VOLUNTEER_RECEIVED);
        donation.setVolunteerConfirmPhoto(confirmPhotoUrl);
        donation.setVolunteerNote(note);
        donation.setVolunteerReceivedAt(LocalDateTime.now());

        ItemDonation saved = itemDonationRepository.save(donation);

        // Donor ကို notify
        // ✅ ဒါနဲ့ အစားထိုး
        String donorMsg;
        if (saved.getAssignedVolunteer() != null) {
            if (saved.getHandoverType() == ItemDonation.HandoverType.DELIVER) {
                donorMsg = "မင်းရဲ့ " + saved.getItemName() + " donation တင်ပြီ။\n" +
                        "📍 Volunteer: " + saved.getAssignedVolunteer().getUsername() + "\n" +
                        "📞 Phone: " + saved.getAssignedVolunteer().getPhoneNumber() + "\n" +
                        "🏘️ Township: " + saved.getDonorTownship() + "\n" +
                        "📅 Date: " + saved.getHandoverDate() + "\n" +
                        "➡️ ဒီ volunteer ဆီ item ပို့ပေးပါ။";
            } else {
                donorMsg = "မင်းရဲ့ " + saved.getItemName() + " donation တင်ပြီ။\n" +
                        "📅 " + saved.getHandoverDate() + " မှာ volunteer လာယူပါမယ်။\n" +
                        "📞 Volunteer: " + saved.getAssignedVolunteer().getPhoneNumber();
            }
        } else {
            donorMsg = saved.getItemName() + " donation တင်ပြီ။ မင်းရဲ့ township မှာ volunteer ရှာနေပါတယ်။";
        }

        notificationService.sendNotification(
                saved.getDonor(),
                "✅ Item Donation Submitted",
                donorMsg,
                Notification.Type.DONATION_RECEIVED,
                saved.getId(),
                "ITEM_DONATION"
        );

        return saved;
    }

    // Admin — stock မှာ သိမ်းပြီဆိုတာ confirm
    @Transactional
    public ItemDonation storeInStock(Long donationId) {
        ItemDonation donation = itemDonationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Item donation not found"));

        donation.setStatus(ItemDonation.Status.STORED_IN_STOCK);

        // Stock မှာ auto add
        stockRepository.findByItemName(donation.getItemName())
                .ifPresent(stock -> {
                    stock.setQuantity(stock.getQuantity() + donation.getQuantity());
                    stockRepository.save(stock);
                });

        return itemDonationRepository.save(donation);
    }

    public ItemDonation rejectItemDonation(Long donationId) {
        ItemDonation donation = itemDonationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Item donation not found"));
        donation.setStatus(ItemDonation.Status.REJECTED);

        ItemDonation saved = itemDonationRepository.save(donation);

        notificationService.sendNotification(
                saved.getDonor(),
                "❌ Item Donation Rejected",
                "မင်းရဲ့ " + saved.getItemName() + " donation ငြင်းပယ်ခံရပြီ။",
                Notification.Type.STATUS_CHANGED,
                saved.getId(),
                "ITEM_DONATION"
        );

        return saved;
    }

    public List<ItemDonation> getMyItemDonations(Long donorId) {
        return itemDonationRepository.findByDonorId(donorId);
    }

    public List<ItemDonation> getByStatus(ItemDonation.Status status) {
        return itemDonationRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public List<ItemDonation> getVolunteerAssigned(Long volunteerId) {
        return itemDonationRepository.findByAssignedVolunteerId(volunteerId);
    }

    public Optional<ItemDonation> getById(Long id) {
        return itemDonationRepository.findById(id);
    }

    public List<ItemDonation> getAll() {
        return itemDonationRepository.findAll();
    }
}