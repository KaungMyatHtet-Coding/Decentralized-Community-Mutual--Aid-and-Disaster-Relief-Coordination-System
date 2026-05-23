package com.hnaungkyoe.service;

import com.hnaungkyoe.entity.User;
import com.hnaungkyoe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 1. အသုံးပြုသူအသစ် ဆောက်ခြင်း (Register/Create User)
    public User registerUser(User user) {
        // စနစ်ထဲမှာ ဒီ Email နဲ့ လူ ရှိနှင့်ပြီးသားလား ကြိုတင်စစ်ဆေးတာပါ
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email '" + user.getEmail() + "' is already taken!");
        }
        return userRepository.save(user);
    }

    // 2. အသုံးပြုသူအားလုံးကို လှမ်းယူခြင်း (Get All Users)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 3. ID အလိုက် အသုံးပြုသူတစ်ယောက်ချင်းစီကို ရှာဖွေခြင်း (Get User By ID)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // 4. Email အလိုက် ရှာဖွေခြင်း (Get User By Email)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 5. အသုံးပြုသူ အချက်အလက် ပြင်ဆင်ခြင်း (Update User)
    public User updateUser(Long id, User updatedUser) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(updatedUser.getUsername());
            user.setEmail(updatedUser.getEmail());
            user.setPhoneNumber(updatedUser.getPhoneNumber());
            user.setRole(updatedUser.getRole());
            user.setVerified(updatedUser.isVerified());
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // 6. အသုံးပြုသူကို ဖျက်ပစ်ခြင်း (Delete User)
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}