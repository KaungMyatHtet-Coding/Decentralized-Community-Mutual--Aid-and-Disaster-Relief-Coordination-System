package com.hnaungkyoe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "aid_requests", indexes = {
        @Index(name = "idx_township", columnList = "township")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AidRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    // ✅ category single field ဖျက်ပြီး categories list ပဲ ထားတယ်
    @ElementCollection(targetClass = Category.class)
    @CollectionTable(name = "aid_request_categories",
            joinColumns = @JoinColumn(name = "aid_request_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private List<Category> categories;

    @Column(nullable = false, length = 100)
    private String township;

    @Column(name = "ward_or_village", nullable = false, length = 150)
    private String wardOrVillage;

    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;

    // ✅ status field ပြန်ထည့်တယ်
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "upvote_count", nullable = false)
    private Long upvoteCount;

    @Column(name = "proof_photo_url", columnDefinition = "TEXT")
    private String proofPhotoUrl;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.upvoteCount == null) this.upvoteCount = 0L;
        if (this.status == null) this.status = Status.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum Category {
        FOOD, MEDICINE, WATER, CLOTHING, SHELTER, OTHER
    }

    public enum Status {
        PENDING, VERIFIED, IN_PROGRESS, RESOLVED, REJECTED
    }
}