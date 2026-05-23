package com.hnaungkyoe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified; // = false ဆိုတဲ့ ပိုနေတဲ့စာသားကို ဖယ်ထုတ်လိုက်လို့ အဝါရောင်လိုင်း ပျောက်သွားပါပြီ

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum Role {
        ROLE_PUBLIC,
        ROLE_VOLUNTEER,
        ROLE_SUB_ADMIN,
        ROLE_SUPER_ADMIN
    }
}