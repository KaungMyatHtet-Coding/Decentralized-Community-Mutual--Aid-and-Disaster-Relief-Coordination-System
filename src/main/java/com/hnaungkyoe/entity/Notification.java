package com.hnaungkyoe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_type", length = 50)
    private String referenceType; // "AID_REQUEST", "DONATION", "CAMPAIGN"

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    public enum Type {
        REQUEST_CREATED,
        STATUS_CHANGED,
        DONATION_RECEIVED,   // ✅ ရှိပြီ
        CAMPAIGN_COMPLETED,  // ➕ ထည့်
        VOLUNTEER_APPROVED,  // ✅ ရှိပြီ
        VOLUNTEER_REJECTED   // ➕ ထည့်
    }
}