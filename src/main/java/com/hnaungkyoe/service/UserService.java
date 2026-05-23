package com.hnaungkyoe.service;

import com.hnaungkyoe.entity.User;
import com.hnaungkyoe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // ← ထည့်လိုက်တယ်

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder; // ← ထည့်လိုက်တယ်
    }

    // 1. အသုံးပြုသူအသစ် ဆောက်ခြင်း (Register)
    public User registerUser(User user) {
        // Email ထပ်နေလားစစ်တယ်
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email '" + user.getEmail() + "' is already taken!");
        }
        // Username ထပ်နေလားစစ်တယ်
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username '" + user.getUsername() + "' is already taken!");
        }
        // ← အဓိက ပြောင်းချက် — Password ကို BCrypt နဲ့ Hash လုပ်ပြီး သိမ်းတယ်
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // 2. အသုံးပြုသူအားလုံး ယူခြင်း
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 3. ID နဲ့ ရှာခြင်း
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // 4. Email နဲ့ ရှာခြင်း
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 5. အချက်အလက် ပြင်ဆင်ခြင်း (Password မပါ — သီးသန့် endpoint သုံးရမယ်)
    public User updateUser(Long id, User updatedUser) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(updatedUser.getUsername());
            user.setEmail(updatedUser.getEmail());
            user.setPhoneNumber(updatedUser.getPhoneNumber());
            user.setRole(updatedUser.getRole());
            user.setVerified(updatedUser.isVerified());
            // ← Password ကို ဒီမှာ မပြောင်းဘူး — သီးသန့် changePassword method သုံးရမယ်
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // 6. Password သီးသန့် ပြောင်းခြင်း
    public User changePassword(Long id, String oldPassword, String newPassword) {
        return userRepository.findById(id).map(user -> {
            // အဟောင်း Password မှန်မမှန် စစ်တယ်
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new RuntimeException("Old password is incorrect!");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // 7. ဖျက်ခြင်း
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}