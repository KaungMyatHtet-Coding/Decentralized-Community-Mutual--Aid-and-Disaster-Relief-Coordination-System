package com.hnaungkyoe.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Secret key — application.properties ထဲ ရွှေ့သင့်တယ်၊ အခုတော့ ဒီမှာပဲ ထားမယ်
    private static final String SECRET = "hnaungkyoe-secret-key-must-be-32-chars!!";
    private static final long EXPIRATION = 1000 * 60 * 60 * 24; // 24 နာရီ

    private Key getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // Token ထုတ်ပေးတယ်
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Token ထဲက Email ထုတ်ယူတယ်
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // Token ထဲက Role ထုတ်ယူတယ်
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // Token မှန်မမှန် စစ်တယ်
    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}