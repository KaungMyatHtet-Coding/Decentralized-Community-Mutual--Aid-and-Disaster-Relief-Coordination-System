package com.hnaungkyoe.security;

import com.hnaungkyoe.entity.User;
import com.hnaungkyoe.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                if (jwtUtil.isTokenValid(token)) {
                    String email = jwtUtil.extractEmail(token);
                    String rawRole = jwtUtil.extractRole(token); // 💡 Token ထဲက Role ကို ယူမယ်

                    // 🎯 (၁) ROLE PREFIX အရှုပ်အထွေးကို ဖြေရှင်းခြင်း
                    // Config ဘက်မှာ ကြိုက်သလိုစစ်လို့ရအောင် Authority ၂ ခုလုံး (Prefix ပါ/မပါ) တစ်ခါတည်း ထည့်ပေးလိုက်မယ်
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                    if (rawRole != null) {
                        String cleanRole = rawRole.replace("ROLE_", ""); // Prefix ပါလာရင် ခွာထုတ်မယ်
                        authorities.add(new SimpleGrantedAuthority(cleanRole));          // ဥပမာ - "SUPER_ADMIN"
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + cleanRole)); // ဥပမာ - "ROLE_SUPER_ADMIN"
                    }

                    // 🎯 (၂) User Object ကို မြန်မြန်ဆန်ဆန် ဆွဲထုတ်ပြီး Context ထဲ ထည့်ခြင်း
                    User user = userRepository.findByEmail(email).orElse(null);

                    if (user != null) {
                        // Spring Security စံနှုန်းအတိုင်း Principal နေရာမှာ User Object ကို စနစ်တကျ သတ်မှတ်ခြင်း
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        user,            // Principal (Controller က @AuthenticationPrincipal နဲ့ ပြန်ယူမည့်အကောင့်)
                                        null,            // Credentials
                                        authorities      // တာဝန်ပေးထားသော Role အားလုံး
                                );

                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception e) {
                // Token Expire ဖြစ်တာ ဒါမှမဟုတ် Error တစ်ခုခုတက်ရင် Security Context ကို ရှင်းထုတ်ပစ်မယ်
                SecurityContextHolder.clearContext();
            }
        }

        // နောက် Filter တစ်ဆင့်ဆီသို့ ဆက်သွားခွင့်ပြုခြင်း
        filterChain.doFilter(request, response);
    }
}