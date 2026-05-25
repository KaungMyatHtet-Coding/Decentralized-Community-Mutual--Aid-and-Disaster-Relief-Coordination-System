package com.hnaungkyoe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "volunteer_applications", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id"})
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class VolunteerApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "proof_image_url", nullable = false, length = 255)
    private String proofImageUrl;

    @Column(name = "operating_township", length = 100) // ← ထည့်
    private String operatingTownship;

    @Column(name = "application_note", nullable = false, columnDefinition = "TEXT")
    private String applicationNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "applied_at", updatable = false)
    private LocalDateTime appliedAt;

    @PrePersist
    protected void onCreate() {
        this.appliedAt = LocalDateTime.now();
        if (this.status == null) this.status = Status.PENDING;
    }

    public enum Status {
        PENDING, APPROVED, REJECTED, FIRED  // ✅ Added FIRED
    }
}