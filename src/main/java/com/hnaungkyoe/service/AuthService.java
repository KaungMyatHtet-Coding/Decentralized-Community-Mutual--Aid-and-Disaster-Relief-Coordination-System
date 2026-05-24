package com.hnaungkyoe.service;

import com.hnaungkyoe.dto.LoginRequest;
import com.hnaungkyoe.dto.LoginResponse;
import com.hnaungkyoe.entity.User;
import com.hnaungkyoe.repository.UserRepository;
import com.hnaungkyoe.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        // Email နဲ့ User ရှာတယ်
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email မှားနေတယ်!"));

        // Password စစ်တယ်
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Password မှားနေတယ်!");
        }

        // Token ထုတ်ပေးတယ်
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new LoginResponse(token, user.getRole().name(), user.getUsername(), user.getId()); // ✨ userId ထည့်လိုက်တာ
    }
}
