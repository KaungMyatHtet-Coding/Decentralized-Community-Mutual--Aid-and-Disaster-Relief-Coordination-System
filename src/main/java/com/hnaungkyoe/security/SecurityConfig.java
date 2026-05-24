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

                        // === SUPER ADMIN ONLY ===
                        .requestMatchers("/api/audit-logs/**")
                        .hasAuthority("ROLE_SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**")
                        .hasAuthority("ROLE_SUPER_ADMIN")

                        // === ADMIN (SUB + SUPER) ===
                        .requestMatchers("/api/stocks/**")
                        .hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_SUB_ADMIN")
                        .requestMatchers("/api/donations/**")
                        .hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_SUB_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/aid-requests/**")
                        .hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_SUB_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/volunteer-applications/**")
                        .hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_SUB_ADMIN")

                        // === ကျန်တဲ့ အားလုံး — Login လိုတယ် ===
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}