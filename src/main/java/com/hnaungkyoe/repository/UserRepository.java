package com.hnaungkyoe.repository;

import com.hnaungkyoe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // နောက်ပိုင်း Login ဝင်ရင် သုံးဖို့ Email နဲ့ အသုံးပြုသူကို လှမ်းရှာမည့် Query လေး ကြိုရေးထားတာပါ
    Optional<User> findByEmail(String email);
}