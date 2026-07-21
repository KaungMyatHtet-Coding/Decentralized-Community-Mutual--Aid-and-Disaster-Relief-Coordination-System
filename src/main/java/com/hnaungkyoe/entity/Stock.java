package com.hnaungkyoe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String itemName;

    @Column(nullable = false, length = 100)
    private String township;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false, length = 50)
    private String unit;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum Category {
        FOOD, MEDICINE, WATER, CLOTHING, SHELTER, OTHER
    }
}