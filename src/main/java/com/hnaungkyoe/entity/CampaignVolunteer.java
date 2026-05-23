package com.hnaungkyoe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaign_volunteers", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"campaign_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignVolunteer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🤝 Many records link to One Campaign
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    // 🤝 Many records link to One User (who must be a Volunteer)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        this.joinedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = Status.PENDING; // ကယ်ဆယ်ရေးခရီးစဉ်မှာ ပါဝင်ဖို့ စာရင်းပေးလိုက်ချင်းမှာ PENDING အဖြစ် Auto သတ်မှတ်ပေးတာပါ
        }
    }

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }
}