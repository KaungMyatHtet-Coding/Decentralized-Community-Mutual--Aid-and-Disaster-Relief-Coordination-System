package com.hnaungkyoe.service;

import com.hnaungkyoe.entity.*;
import com.hnaungkyoe.repository.UserRepository;
import com.hnaungkyoe.repository.VolunteerApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class VolunteerApplicationService {

    @Autowired private VolunteerApplicationRepository repository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationService notificationService;

    public VolunteerApplication apply(VolunteerApplication application) {
        // တစ်ယောက်တည်း ထပ်ထပ် မလျှောက်နိုင်အောင် စစ်တယ်
        if (repository.existsByUserId(application.getUser().getId())) {
            throw new RuntimeException("You have already submitted a volunteer application!");
        }
        return repository.save(application);
    }

    @Transactional
    public VolunteerApplication approveApplication(Long applicationId) {
        VolunteerApplication app = repository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        app.setStatus(VolunteerApplication.Status.APPROVED);

        // User role ကို VOLUNTEER အဖြစ် ပြောင်းတယ်
        User user = app.getUser();
        user.setRole(User.Role.ROLE_VOLUNTEER);
        user.setVerified(true);
        userRepository.save(user);

        VolunteerApplication saved = repository.save(app);

        // Notify လုပ်တယ်
        notificationService.sendNotification(
                user,
                "Volunteer Application Approved",
                "မင်းရဲ့ volunteer လျှောက်လွှာ အတည်ပြုပြီ။ ကြိုဆိုပါတယ်!",
                Notification.Type.VOLUNTEER_APPROVED,
                saved.getId(),
                "VOLUNTEER_APPLICATION"
        );

        return saved;
    }

    public VolunteerApplication rejectApplication(Long applicationId) {
        VolunteerApplication app = repository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        app.setStatus(VolunteerApplication.Status.REJECTED);
        VolunteerApplication saved = repository.save(app);

        // ✅ ဒါပဲ ထည့် — reject notification
        notificationService.sendNotification(
                saved.getUser(),
                "Volunteer Application Rejected",
                "မင်းရဲ့ volunteer လျှောက်လွှာ ငြင်းပယ်ခံရပြီ။ နောက်တစ်ကြိမ် ထပ်လျှောက်နိုင်ပါတယ်။",
                Notification.Type.VOLUNTEER_REJECTED,
                saved.getId(),
                "VOLUNTEER_APPLICATION"
        );

        return saved;
    }

    public List<VolunteerApplication> getAllApplications() {
        return repository.findAll();
    }

    public List<VolunteerApplication> getPendingApplications() {
        return repository.findByStatus(VolunteerApplication.Status.PENDING);
    }

    public Optional<VolunteerApplication> getApplicationById(Long id) {
        return repository.findById(id);
    }
}