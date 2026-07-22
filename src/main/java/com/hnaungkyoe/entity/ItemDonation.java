package com.hnaungkyoe.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "item_donations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDonation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 💡 Frontend ကို အလှူရှင်အချက်အလက် ပေးဖတ်ဖို့ WRITE_ONLY အစားထိုးသုံးပါမယ်
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private User donor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // API ကနေ ပြန်ထုတ်ပြရင် မြင်ရအောင် ဖတ်ခွင့်ပေးလိုက်ပါမယ်
    private Campaign campaign;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false, length = 50)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_condition", nullable = false)
    private Condition condition;

    @Column(name = "donor_township", nullable = false, length = 100)
    private String donorTownship;

    @Column(name = "donor_district", length = 100)
    private String donorDistrict;

    @Column(name = "donor_division", length = 100)
    private String donorDivision;

    @Column(name = "donor_phone", nullable = false, length = 20)
    private String donorPhone;

    @Column(name = "street_address", columnDefinition = "TEXT")
    private String streetAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "handover_type", nullable = false)
    private HandoverType handoverType;

    @Column(name = "handover_date")
    private LocalDateTime handoverDate;

    @Column(name = "item_photo_url", length = 255)
    private String itemPhotoUrl;

    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous;

    // 💡 စာရင်းထုတ်ပြရင် Volunteer အမည်ပါ မြင်ရအောင် လုပ်ထားပါတယ်
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_volunteer_id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private User assignedVolunteer;

    @Column(name = "volunteer_confirm_photo", length = 255)
    private String volunteerConfirmPhoto;

    @Column(name = "volunteer_received_at")
    private LocalDateTime volunteerReceivedAt;

    @Column(name = "volunteer_note", columnDefinition = "TEXT")
    private String volunteerNote;

    @Column(name = "volunteer_accepted_at")
    private LocalDateTime volunteerAcceptedAt;

    @Column(name = "volunteer_reject_reason", length = 255)
    private String volunteerRejectReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Condition {
        NEW, GOOD, FAIR
    }

    public enum HandoverType {
        DELIVER, PICKUP
    }

    public enum Status {
        PENDING_ADMIN,
        ADMIN_APPROVED,
        ADMIN_REJECTED,
        ASSIGNED_TO_VOLUNTEER, // 💡 ဒါကို အောက်က repository မှာ ဒီအတိုင်း သုံးပါမယ်
        VOLUNTEER_ACCEPTED,
        VOLUNTEER_REJECTED,
        VOLUNTEER_RECEIVED,
        STORED_IN_STOCK,
        COMPLETED
    }

    @Column(name = "otp_code", length = 10)
    private String otpCode;

    @Column(name = "otp_verified_at")
    private LocalDateTime otpVerifiedAt;

    @Column(name = "stored_at")
    private LocalDateTime storedAt;

    @Column(name = "senior_volunteer_id")
    private Long seniorVolunteerId;

    @Column(name = "condition_notes", columnDefinition = "TEXT")
    private String conditionNotes;

    @Transient
    @JsonProperty("campaignId")
    private Long campaignId;
}