package com.cs.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {

    private static final String SECRET = "venve-jwt-secret-key-2026-min-256-bits!!";
    private static final long EXPIRE_MS = 24 * 60 * 60 * 1000L;

    private static SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public static String generate(Long userId, String userName, String role) {
        String actualRole = role != null ? role : "USER";
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims(Map.of("userName", userName, "role", actualRole))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_MS))
                .signWith(getKey())
                .compact();
    }

    public static Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static Long getUserId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }

    public static String getUserName(String token) {
        return parse(token).get("userName", String.class);
    }

    public static String getRole(String token) {
        String role = parse(token).get("role", String.class);
        return role != null ? role : "USER";
    }

    public static boolean validate(String token) {
        if (token == null || token.isEmpty()) return false;
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
