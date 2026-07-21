package com.hnaungkyoe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_usage_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "township", nullable = false)
    private String township;

    @Column(nullable = false)
    private Double quantityUsed;

    @Column(columnDefinition = "TEXT")
    private String reasonDetails;

    @Column(name = "proof_photo_url")
    private String proofPhotoUrl;

    @Column(name = "authorized_by")
    private String authorizedBy;

    @Column(name = "usage_date")
    private LocalDateTime usageDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.usageDate == null) {
            this.usageDate = LocalDateTime.now();
        }
    }
}
