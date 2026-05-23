package com.hnaungkyoe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🤝 Many notifications belong to One User
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
    private Long referenceId; // Aid Request ID သို့မဟုတ် Donation ID ကို သိမ်းရန်

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false; // Notification စဝင်ချင်းမှာ အလိုအလျောက် "မဖတ်ရသေးပါ (Unread)" အဖြစ် သတ်မှတ်တာပါ
    }

    public enum Type {
        REQUEST_CREATED,
        STATUS_CHANGED,
        VOLUNTEER_APPROVED,
        DONATION_RECEIVED
    }
}