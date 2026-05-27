package com.hnaungkyoe.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    // 💡 တကယ့် Production မှာတော့ application.properties ထဲ ရွှေ့ရပါမယ်ဗျာ
    // Modern JJWT ရဲ့ HS256 အတွက် အနည်းဆုံး 256-bit (32 characters) ရှိရပါမယ်။
    private static final String SECRET = "hnaungkyoe-secret-key-must-be-32-chars!!";
    private static final long EXPIRATION = 1000 * 60 * 60 * 24; // 24 နာရီ

    private SecretKey getKey() {
        // StandardCharsets.UTF_8 သုံးတာက ပိုပြီး စိတ်ချရပါတယ်ဗျာ
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    // Token ထုတ်ပေးသည်
    public String generateToken(String email, String role) {
        // 💡 Modern JJWT (0.12+) မှာ builder ရဲ့ method တွေ ပြောင်းလဲသွားပါတယ်
        return Jwts.builder()
                .subject(email)            // setSubject အစား subject()
                .claim("role", role)
                .issuedAt(new Date())      // setIssuedAt အစား issuedAt()
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION)) // setExpiration အစား expiration()
                .signWith(getKey())        // Algorithm ကို ခေတ်သစ်မှာ Key size အပေါ်မူတည်ပြီး အလိုအလျောက် သတ်မှတ်ပေးပါတယ်
                .compact();
    }

    // Token ထဲက Email ထုတ်ယူသည်
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // Token ထဲက Role ထုတ်ယူသည်
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // Token မှန်မမှန် စစ်သည်
    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Token သက်တမ်းကုန်တာ (Expired) ဒါမှမဟုတ် Signature မှားတာတွေကို ဒီမှာ ဖမ်းပေးသွားမှာပါ
            return false;
        }
    }

    private Claims getClaims(String token) {
        // 💡 Modern JJWT (0.12+) မှာ parserBuilder() အစား parser() ကို တိုက်ရိုက်သုံးပါတယ်
        return Jwts.parser()
                .verifyWith(getKey()) // setSigningKey အစား verifyWith()
                .build()
                .parseSignedClaims(token) // parseClaimsJws အစား parseSignedClaims()
                .getPayload();            // getBody အစား getPayload()
    }
}