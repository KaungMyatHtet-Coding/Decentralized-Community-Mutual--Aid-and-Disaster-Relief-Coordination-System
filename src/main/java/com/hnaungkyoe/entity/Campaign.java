package com.hnaungkyoe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaigns")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "title_my", length = 150)
    private String titleMy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "description_my", columnDefinition = "TEXT")
    private String descriptionMy;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(length = 50)
    private String category;

    @Column(name = "target_amount", precision = 15, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "author_id")
    private Long authorId;

    @Column(name = "township", length = 100)
    private String township;

    @Column(name = "current_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentAmount;

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
        if (this.currentAmount == null) this.currentAmount = BigDecimal.ZERO;
        if (this.status == null) this.status = Status.ACTIVE;
    }

    public enum Status {
        PENDING, ACTIVE, COMPLETED, CANCELLED, REJECTED
    }
}