package util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JWTUtils {
    // Option A: base64-encoded secret (recommended)
    // export JWT_SECRET_B64=<base64 of 32+ random bytes>
    private static final String SECRET_B64 = System.getenv("JWT_SECRET_B64");


    private static Key key() {
        if (SECRET_B64 != null && !SECRET_B64.isBlank()) {
            byte[] keyBytes = Decoders.BASE64.decode(SECRET_B64);
            return Keys.hmacShaKeyFor(keyBytes);
        }
        // Option B: raw string secret (must be >= 32 chars for HS256)
        String raw = System.getenv("JWT_SECRET");
        if (raw == null || raw.length() < 32) {
            // fallback for dev only; replace with a proper secret
            raw = "dev-secret-must-be-at-least-32-chars!!";
        }
        return Keys.hmacShaKeyFor(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken(int userId, String email) {
        long expirationMs = 24L * 60 * 60 * 1000; // 24h
        return Jwts.builder()
            .setSubject(String.valueOf(userId))
            .claim("email", email)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(key(), SignatureAlgorithm.HS256)
            .compact();
    }

    public static Claims parseClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public static boolean isExpired(Claims claims) {
        return claims.getExpiration() == null || claims.getExpiration().before(new Date());
    }
}
