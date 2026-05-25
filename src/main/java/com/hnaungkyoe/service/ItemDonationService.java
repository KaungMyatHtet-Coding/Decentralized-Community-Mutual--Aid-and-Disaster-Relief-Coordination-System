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

        // Donor township နဲ့ match တဲ့ volunteer ရှာ
        List<VolunteerApplication> volunteers = volunteerApplicationRepository
                .findByStatusAndOperatingTownship(
                        VolunteerApplication.Status.APPROVED,
                        donation.getDonorTownship()
                );

        // Township match တဲ့ volunteer ရှိရင် auto assign
        if (!volunteers.isEmpty()) {
            User assignedVolunteer = volunteers.get(0).getUser();
            donation.setAssignedVolunteer(assignedVolunteer);

            // Volunteer ကို notify
            notificationService.sendNotification(
                    assignedVolunteer,
                    "📦 Item Donation Assigned",
                    donation.getDonorTownship() + " မှ " +
                            donation.getItemName() + " (" + donation.getQuantity()
                            + " " + donation.getUnit() + ") လက်ခံပေးပါ",
                    Notification.Type.STATUS_CHANGED,
                    null,
                    "ITEM_DONATION"
            );
        }

        ItemDonation saved = itemDonationRepository.save(donation);

        // Donor ကို confirm notification
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
        notificationService.sendNotification(
                saved.getDonor(),
                "📦 Donation Received by Volunteer",
                "မင်းရဲ့ " + saved.getItemName() +
                        " ကို volunteer လက်ခံပြီ ✅",
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