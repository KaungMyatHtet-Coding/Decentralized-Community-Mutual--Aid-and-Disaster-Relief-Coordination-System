package com.hnaungkyoe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "donations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🤝 Many donations can be made by One User (If null, it means Anonymous Donor)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User donor;

    // 🤝 Many donations can go to One Campaign
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonationType donationType;

    // ငွေကြေးလှူဒါန်းမှုအတွက်သုံးရန် (DonationType က MONEY ဆိုရင် ဖြည့်ရမည်)
    @Column(name = "amount")
    private Double amount;

    // ပစ္စည်းလှူဒါန်းမှုအတွက်သုံးရန် (DonationType က ITEMS ဆိုရင် ဖြည့်ရမည်)
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
        if (this.status == null) {
            this.status = Status.PENDING; // အလှူရှင်က ဖြတ်ပိုင်းတင်လိုက်ချင်းမှာ PENDING အဖြစ် Auto သတ်မှတ်ပေးတာပါ
        }
    }

    public enum DonationType {
        MONEY,
        ITEMS
    }

    public enum Status {
        PENDING,
        CONFIRMED,
        REJECTED
    }
}