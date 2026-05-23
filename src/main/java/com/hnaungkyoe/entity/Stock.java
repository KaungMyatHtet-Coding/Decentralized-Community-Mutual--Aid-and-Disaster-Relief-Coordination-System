package com.hnaungkyoe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false, length = 50)
    private String unit; // ဥပမာ - "အိတ်", "ကတ်", "ဗူး", "ထုပ်" စသဖြင့် သတ်မှတ်ရန်

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(); // ပစ္စည်းစာရင်း အသစ်ဝင်တာပဲဖြစ်ဖြစ်၊ အရေအတွက် ပြောင်းလဲတာပဲဖြစ်ဖြစ် အချိန်ကို Auto မှတ်ပေးမှာပါ
    }

    public enum Category {
        FOOD,
        MEDICINE,
        WATER,
        CLOTHING,
        SHELTER,
        OTHER
    }
}