package com.hnaungkyoe.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // === 🔓 CORS Configuration (Frontend နဲ့ ချိတ်ဆက်ရန်) ===
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:5173"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))

                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 🔥 JWT Filter ကို စစ်ဆေးမှုအားလုံးရဲ့ ရှေ့ဆုံးမှာ ထားရှိခြင်း
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth

                        // === 🌐 PUBLIC ENDPOINTS (Token မလိုဘဲ ဝင်ခွင့်ပြုမည်) ===
                        .requestMatchers("/api/users/register").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/upload/**").permitAll()

                        // Aid Requests (GET standard ဟာ public ဖြစ်တယ်)
                        .requestMatchers(HttpMethod.GET, "/api/aid-requests").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/aid-requests/**").permitAll()

                        // Campaigns (GET standard ဟာ public ဖြစ်တယ်)
                        .requestMatchers(HttpMethod.GET, "/api/campaigns").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/campaigns/**").permitAll()

                        // Posts/NewsFeed (GET standard ဟာ public ဖြစ်တယ်)
                        .requestMatchers(HttpMethod.GET, "/api/posts").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()


                        // === 🛡️ ADMIN ONLY ENDPOINTS (SUPER + SUB ADMIN သာ) ===
                        // Campaigns & Posts Management
                        .requestMatchers(HttpMethod.POST, "/api/campaigns").hasAnyAuthority("ROLE_SUPER_ADMIN","SUPER_ADMIN","ROLE_SUB_ADMIN","SUB_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/campaigns/**").hasAnyAuthority("ROLE_SUPER_ADMIN","SUPER_ADMIN","ROLE_SUB_ADMIN","SUB_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/campaigns/**").hasAnyAuthority("ROLE_SUPER_ADMIN","SUPER_ADMIN","ROLE_SUB_ADMIN","SUB_ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/posts/all").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/posts").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/posts/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")

                        // Dashboard Stats & Volunteer Control
                        .requestMatchers(HttpMethod.GET, "/api/admin/stats").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/admin/volunteers").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/admin/volunteers/applications").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/admin/volunteers/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")

                        // Donation, Aid and Stocks Approval
                        .requestMatchers(HttpMethod.GET, "/api/donations").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/donations/").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/donations/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/donations/filter").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/donations/stats").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/aid-requests/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")
                        .requestMatchers("/api/stocks/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")


                        // === 🎖️ SUPER ADMIN ONLY ENDPOINTS ===
                        .requestMatchers("/api/audit-logs/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN")


                        // === 🤝 MIXED ACCESS (ADMIN + VOLUNTEER) ===
                        .requestMatchers(HttpMethod.PATCH, "/api/item-donations/**")
                        .hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN", "ROLE_VOLUNTEER")
                        .requestMatchers(HttpMethod.PATCH, "/api/volunteer-applications/**")
                        .hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")


                        // === 🔔 NOTIFICATION ENDPOINTS (✨ တိုးထည့်လိုက်သော အပိုင်းသစ်) ===
                        // User, Volunteer, Admin အားလုံး Login ဝင်ထားရင် Noti ဖတ်ခွင့်/ကြည့်ခွင့် ရှိစေရမည်
                        .requestMatchers(HttpMethod.GET, "/api/notifications/my").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/notifications/my/unread").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/notifications/**").authenticated() // 💡 PUT Method တစ်ခုချင်း/အားလုံး Read မလုပ်နိုင်တဲ့ Error ကို ဖြေရှင်းပေးထားပါတယ်


                        // === 👥 USER AUTHENTICATED ENDPOINTS (အလှူရှင်နှင့် စေတနာ့ဝန်ထမ်းများ) ===
                        .requestMatchers(HttpMethod.GET, "/api/donations/my").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/donations").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/item-donations").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/item-donations/my").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/item-donations/assigned").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/volunteer-applications").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/volunteer-applications/my").authenticated()


                        // === 🔒 ကျန်ရှိသမျှ အားလုံးသည် Login (Token) မရှိမဖြစ် လိုအပ်သည် ===
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}