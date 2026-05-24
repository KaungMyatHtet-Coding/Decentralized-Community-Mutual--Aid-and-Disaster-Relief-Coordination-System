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
import org.springframework.web.cors.CorsConfiguration; // <-- အသစ်သွင်းလိုက်သော Class ✨
import java.util.List; // <-- အသစ်သွင်းလိုက်သော Class ✨

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
                // === 🔓 CORS ကို Spring Security ကနေ တံခါးဖွင့်ပေးလိုက်ခြင်း ===
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:5173")); // Frontend URL သတ်မှတ်ခြင်း
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))

                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // === PUBLIC — Token မလိုဘဲ ဝင်လို့ရတယ် ===
                        .requestMatchers("/api/users/register").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()

                        // Aid Requests — GET ပဲ public ဖြစ်တယ်
                        .requestMatchers(HttpMethod.GET, "/api/aid-requests").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/aid-requests/**").permitAll()

                        // Campaigns — GET ပဲ public ဖြစ်တယ်
                        .requestMatchers(HttpMethod.GET, "/api/campaigns").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/campaigns/**").permitAll()

                        // === USER & ADMIN (Authenticated Users) ===
                        // 💡 အလှူမှတ်တမ်းကြည့်ခြင်းနှင့် လှူဒါန်းခြင်းကို ပထမဦးစားပေး အနေဖြင့် သီးသန့်ခွဲထုတ်ထားခြင်း
                        .requestMatchers(HttpMethod.GET, "/api/donations/my").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/donations").authenticated()

                        // ဒါတွေ .authorizeHttpRequests ထဲမှာ တိုးထည့်ပါ
                        .requestMatchers(HttpMethod.GET, "/api/donations/filter")
                        .hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/donations/stats")
                        .hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")

                        // === SUPER ADMIN ONLY ===
                        .requestMatchers("/api/audit-logs/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN")

                        // === ADMIN (SUB + SUPER) ===
                        .requestMatchers("/api/stocks/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")

                        // 💡 Admin သီးသန့် အလှူစာရင်းကြည့်ခြင်းနှင့် Confirm/Reject လုပ်ခြင်း
                        // (အပေါ်က /my လမ်းကြောင်းနဲ့ မရောထွေးစေရန် သေချာစေပါသည်)
                        .requestMatchers(HttpMethod.GET, "/api/donations").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/donations/").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/donations/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/aid-requests/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/volunteer-applications/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_SUB_ADMIN", "SUB_ADMIN")

                        // === ကျန်တဲ့ အားလုံး — Login လိုတယ် ===
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}