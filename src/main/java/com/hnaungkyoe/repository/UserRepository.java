package com.hnaungkyoe.repository;

import com.hnaungkyoe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Email နဲ့ ရှာတယ် (Login အတွက်)
    Optional<User> findByEmail(String email);

    // Username နဲ့ ရှာတယ် (Register duplicate check အတွက်)
    Optional<User> findByUsername(String username);

    // Email ရှိမရှိ စစ်တယ်
    boolean existsByEmail(String email);

    // Username ရှိမရှိ စစ်တယ်
    boolean existsByUsername(String username);
}