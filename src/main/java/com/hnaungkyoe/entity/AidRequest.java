package com.hnaungkyoe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "aid_requests", indexes = {
        @Index(name = "idx_township", columnList = "township"),
        @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AidRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🤝 Many Aid Requests belong to One User (Reporter)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String township;

    @Column(name = "ward_or_village", nullable = false, length = 150)
    private String wardOrVillage;

    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "upvote_count", nullable = false)
    private Long upvoteCount;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.upvoteCount == null) {
            this.upvoteCount = 0L; // ပို့စ်စတင်ဆောက်ချိန်မှာ Upvote Count ကို 0 အဖြစ် Auto သတ်မှတ်ပေးတာပါ
        }
    }

    public enum Category {
        FOOD,
        MEDICINE,
        WATER,
        CLOTHING,
        SHELTER,
        OTHER
    }

    public enum Status {
        PENDING,
        VERIFIED,
        RESOLVED
    }
}