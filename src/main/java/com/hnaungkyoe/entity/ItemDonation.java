package com.hnaungkyoe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "item_donations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ItemDonation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ဘယ် user က donate လုပ်တယ်
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    // ဘယ် campaign အတွက်
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    // Item details
    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false, length = 50)
    private String unit;

    // ✅ ပြင် — column name သတ်မှတ်
    @Enumerated(EnumType.STRING)
    @Column(name = "item_condition", nullable = false)
    private Condition condition;

    // Donor location
    @Column(name = "donor_township", nullable = false, length = 100)
    private String donorTownship;

    @Column(name = "donor_phone", nullable = false, length = 20)
    private String donorPhone;

    // Handover details
    @Enumerated(EnumType.STRING)
    @Column(name = "handover_type", nullable = false)
    private HandoverType handoverType;

    @Column(name = "handover_date")
    private LocalDateTime handoverDate;

    // Donor proof photo
    @Column(name = "item_photo_url", length = 255)
    private String itemPhotoUrl;

    // Anonymous toggle
    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous;

    // Assigned volunteer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_volunteer_id")
    private User assignedVolunteer;

    // Volunteer confirmation
    @Column(name = "volunteer_confirm_photo", length = 255)
    private String volunteerConfirmPhoto;

    @Column(name = "volunteer_received_at")
    private LocalDateTime volunteerReceivedAt;

    @Column(name = "volunteer_note", columnDefinition = "TEXT")
    private String volunteerNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = Status.PENDING_VOLUNTEER;
        if (this.isAnonymous == null) this.isAnonymous = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum Condition {
        NEW, GOOD, FAIR
    }

    public enum HandoverType {
        DELIVER, // donor ပို့မယ်
        PICKUP   // volunteer လာယူမယ်
    }

    public enum Status {
        PENDING_VOLUNTEER,   // volunteer မရောက်သေးဘူး
        VOLUNTEER_RECEIVED,  // volunteer လက်ခံပြီ
        STORED_IN_STOCK,     // stock မှာ သိမ်းပြီ
        REJECTED             // ငြင်းပယ်
    }
}