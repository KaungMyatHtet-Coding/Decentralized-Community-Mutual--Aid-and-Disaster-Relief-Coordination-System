package com.hnaungkyoe.service;

import com.hnaungkyoe.dto.LoginRequest;
import com.hnaungkyoe.dto.LoginResponse;
import com.hnaungkyoe.entity.User;
import com.hnaungkyoe.repository.UserRepository;
import com.hnaungkyoe.repository.PasswordResetTokenRepository;
import com.hnaungkyoe.entity.PasswordResetToken;
import com.hnaungkyoe.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    
    @Autowired
    private EmailService emailService;

    public LoginResponse login(LoginRequest request) {
        // Email နဲ့ User ရှာတယ်
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email မှားနေတယ်!"));

        // Check if user is active
        if (user.getIsActive() != null && !user.getIsActive()) {
            throw new RuntimeException("သင့်အကောင့် ပိတ်ပင်ခံထားရပါသည်။ (Account Deactivated)");
        }

        // Password စစ်တယ်
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Password မှားနေတယ်!");
        }

        // Token ထုတ်ပေးတယ်
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new LoginResponse(token, user.getRole().name(), user.getUsername(), user.getId(),user.getProfileCompleted()); // ✨ userId ထည့်လိုက်တာ
    }

    @Transactional
    public void processForgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found in our system."));

        // Delete any existing token
        tokenRepository.deleteByUser(user);

        // Generate token
        String token = UUID.randomUUID().toString();
        
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
                
        tokenRepository.save(resetToken);

        // Send Email
        String resetLink = "http://localhost:5173/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or missing token."));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Token has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete token after successful reset
        tokenRepository.delete(resetToken);
    }
}
