package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.User;
import com.hnaungkyoe.repository.UserRepository;
import com.hnaungkyoe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // Frontend ဘက်က လှမ်းခေါက်ရင် Block မဖြစ်အောင် ကြိုဖွင့်ပေးထားတာပါ
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 1. Register User (POST http://localhost:8081/api/users/register)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            return ResponseEntity.ok(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. Get All Users (GET http://localhost:8081/api/users)
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // 3. Get User By ID (GET http://localhost:8081/api/users/{id})
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. Update User (PUT http://localhost:8081/api/users/{id})
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        try {
            User user = userService.updateUser(id, updatedUser);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 5. Delete User (DELETE http://localhost:8081/api/users/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        try {
            userService.deleteUser(id, currentUser);
            return ResponseEntity.ok("User deleted successfully with id: " + id);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 6. Change Role (PATCH http://localhost:8081/api/users/{id})
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody java.util.Map<String, String> updates, @AuthenticationPrincipal User currentUser) {
        try {
            if (updates.containsKey("role")) {
                User user = userService.updateUserRole(id, User.Role.valueOf(updates.get("role")), currentUser);
                return ResponseEntity.ok(user);
            }
            return ResponseEntity.badRequest().body("Role not provided");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @Autowired private UserRepository userRepository;
    @GetMapping("/volunteers")
    public ResponseEntity<List<User>> getAllVolunteers(@AuthenticationPrincipal User currentUser) {
        List<User.Role> volRoles = List.of(User.Role.ROLE_VOLUNTEER, User.Role.ROLE_SENIOR_VOLUNTEER);
        List<User> volunteers;
        
        if (currentUser != null && currentUser.getRole() == User.Role.ROLE_SUB_ADMIN) {
            volunteers = userRepository.findByTownshipAndRoleIn(currentUser.getTownship(), volRoles);
        } else {
            volunteers = userRepository.findByRoleIn(volRoles);
        }
        return ResponseEntity.ok(volunteers);
    }
}