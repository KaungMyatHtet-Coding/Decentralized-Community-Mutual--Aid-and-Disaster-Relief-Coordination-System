package com.hnaungkyoe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Volunteer submits this after completing a delivery.
 * Admin reviews, approves → stock auto-deducted, AidRequest → RESOLVED.
 */
@Entity
@Table(name = "delivery_reports")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aid_request_id", nullable = false)
    private AidRequest aidRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id", nullable = false)
    private User volunteer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    // ── Delivered items (embedded list) ──────────────────────────
    @ElementCollection
    @CollectionTable(
        name = "delivery_report_items",
        joinColumns = @JoinColumn(name = "delivery_report_id")
    )
    private List<DeliveredItemEntry> deliveredItems;

    @Column(name = "proof_photo_url", columnDefinition = "TEXT")
    private String proofPhotoUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING_REVIEW;

    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
        if (this.status == null) this.status = Status.PENDING_REVIEW;
    }

    // ── Embedded: one item in the delivery ──────────────────────
    @Embeddable
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DeliveredItemEntry {
        @Column(name = "item_name", nullable = false, length = 100)
        private String itemName;

        @Column(name = "quantity_delivered", nullable = false)
        private Double quantityDelivered;

        @Column(nullable = false, length = 30)
        private String unit;
    }

    public enum Status {
        PENDING_REVIEW,
        APPROVED,
        REJECTED
    }
}
