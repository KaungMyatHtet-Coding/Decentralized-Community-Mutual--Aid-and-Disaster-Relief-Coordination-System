package com.hnaungkyoe.service;

import com.hnaungkyoe.entity.Notification;
import com.hnaungkyoe.entity.User;
import com.hnaungkyoe.repository.UserRepository;
import com.hnaungkyoe.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class NotificationService {

    @Autowired private NotificationRepository notificationRepository;

    // Internal method — Service တွေက ခေါ်သုံးဖို့
    public void sendNotification(User user, String title, String message,
                                 Notification.Type type, Long referenceId,
                                 String referenceType) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();
        notificationRepository.save(notification);
    }

    public List<Notification> getUnreadNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    public List<Notification> getAllNotificationsForUser(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    public Notification markOneAsRead(Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        n.setRead(true);
        return notificationRepository.save(n);
    }
    @Autowired private UserRepository userRepository;

    // ➕ Admin အားလုံးဆီ notification ပို့တဲ့ method အသစ်
    public void sendToAllAdmins(String title, String message,
                                Notification.Type type, Long referenceId,
                                String referenceType) {
        List<User> admins = userRepository.findByRoleIn(
                List.of(User.Role.ROLE_SUPER_ADMIN, User.Role.ROLE_SUB_ADMIN)
        );
        for (User admin : admins) {
            sendNotification(admin, title, message, type, referenceId, referenceType);
        }
    }
    // ➕ Users အားလုံးဆီ notification ပို့တဲ့ method
    public void sendToAllUsers(String title, String message,
                               Notification.Type type, Long referenceId,
                               String referenceType) {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            sendNotification(user, title, message, type, referenceId, referenceType);
        }
    }
}