package com.hnaungkyoe.repository;

import com.hnaungkyoe.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // အသုံးပြုသူတစ်ယောက်ချင်းစီအလိုက် မဖတ်ရသေးတဲ့ Notification တွေကို လှမ်းယူမည့်အကွက်လေးပါ
    List<Notification> findByUserIdAndIsReadFalse(Long userId);
}