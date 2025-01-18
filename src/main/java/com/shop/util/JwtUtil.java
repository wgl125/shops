package com.shop.util;

import com.shop.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.nio.charset.StandardCharsets;

public class JwtUtil {
    private static final String SECRET_KEY = "student_shops_secret_key_2025_01_06_very_long_and_secure";
    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRATION = 24 * 60 * 60 * 1000; // 24小时

    public static String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key)
                .compact();
    }
    
    public static String generateToken(User user) {
        return generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static User verifyToken(String token) {
        try {
            Claims claims = parseToken(token);
            User user = new User();
            user.setId(Long.parseLong(claims.getSubject()));
            user.setUsername(claims.get("username", String.class));
            user.setRole(claims.get("role", String.class));
            return user;
        } catch (Exception e) {
            return null;
        }
    }
}
