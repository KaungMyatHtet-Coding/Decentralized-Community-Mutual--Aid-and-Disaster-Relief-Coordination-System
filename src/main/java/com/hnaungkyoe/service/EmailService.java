package com.hnaungkyoe.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("hnaungkyoe.community@gmail.com"); // Can be anything, gmail overrides it with your authenticated email
        message.setTo(toEmail);
        message.setSubject("Hnaung Kyoe - Password Reset Request");
        message.setText("Hello,\n\nYou have requested to reset your password. Please click the link below to set a new password:\n\n" 
                + resetLink + "\n\nThis link will expire in 24 hours.\n\nIf you did not request this, please ignore this email.\n\nThank you,\nHnaung Kyoe Team");

        mailSender.send(message);
    }
}
