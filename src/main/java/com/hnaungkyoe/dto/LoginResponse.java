package com.hnaungkyoe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String role;
    private String username;
    private Long userId; // ✨ အသစ်တိုးလိုက်တာ
    private Boolean profileCompleted;
}