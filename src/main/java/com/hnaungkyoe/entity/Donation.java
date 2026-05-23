package com.hnaungkyoe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "donations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nullable — anonymous donor ဖြစ်နိုင်တယ်
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User donor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonationType donationType;

    // BigDecimal — ငွေကြေးအတွက် Double မသုံးသင့်
    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "item_name", length = 100)
    private String itemName;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "proof_image_url", nullable = false, length = 255)
    private String proofImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "donated_at", updatable = false)
    private LocalDateTime donatedAt;

    @PrePersist
    protected void onCreate() {
        this.donatedAt = LocalDateTime.now();
        if (this.status == null) this.status = Status.PENDING;
    }

    public enum DonationType {
        MONEY, ITEMS
    }

    public enum Status {
        PENDING, CONFIRMED, REJECTED
    }
}