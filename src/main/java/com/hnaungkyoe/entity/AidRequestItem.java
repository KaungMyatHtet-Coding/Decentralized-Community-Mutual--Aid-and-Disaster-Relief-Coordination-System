package com.hnaungkyoe.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents one line-item in an Aid Request.
 * e.g.  50 kg rice,  100 bottles water
 */
@Entity
@Table(name = "aid_request_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AidRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aid_request_id", nullable = false)
    private AidRequest aidRequest;

    @Column(nullable = false, length = 100)
    private String itemName;

    @Column(nullable = false)
    private Double quantity;

    /** kg / pcs / bottles / packets / meals / liters */
    @Column(nullable = false, length = 30)
    private String unit;
}
