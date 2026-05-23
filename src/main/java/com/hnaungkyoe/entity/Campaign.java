package com.hnaungkyoe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaigns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "target_amount", nullable = false)
    private Double targetAmount;

    @Column(name = "current_amount", nullable = false)
    private Double currentAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.currentAmount == null) {
            this.currentAmount = 0.0; // Campaign စဆောက်ချင်းမှာ လက်ရှိရရှိငွေကို 0.0 အဖြစ် Auto သတ်မှတ်ပေးတာပါ
        }
        if (this.status == null) {
            this.status = Status.ACTIVE; // ပုံမှန်အားဖြင့် စဖွင့်ချင်းမှာ ACTIVE အဖြစ် ထားရှိမှာပါ
        }
    }

    public enum Status {
        ACTIVE,
        COMPLETED,
        CANCELLED
    }
}