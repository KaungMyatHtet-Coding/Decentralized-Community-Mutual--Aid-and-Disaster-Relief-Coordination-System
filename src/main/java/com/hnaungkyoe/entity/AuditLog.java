package com.hnaungkyoe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🤝 Many logs can be performed by One User (Admin/Staff)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", nullable = false)
    private User performedBy;

    @Column(nullable = false, length = 100)
    private String action; // e.g., "UPDATED_REQUEST_STATUS", "APPROVED_VOLUNTEER"

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // e.g., "AID_REQUEST", "DONATION"

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue; // ပြောင်းလဲခြင်းမပြုမီက ဒေတာအဟောင်း

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue; // ပြောင်းလဲလိုက်သည့် ဒေတာအသစ်

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}