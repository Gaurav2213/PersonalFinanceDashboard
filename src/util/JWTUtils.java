package util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

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
    	// In milliseconds (for token lifetime)
        long expirationMs = 30 * 60 * 1000; // 30mints
        String jti = UUID.randomUUID().toString();
  
        return Jwts.builder()
            .setSubject(String.valueOf(userId))
            .claim("email", email)
            .setId(jti)  
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

    //check the expiry of the token 
    public static boolean isExpired(Claims claims) {
        return claims.getExpiration() == null || claims.getExpiration().before(new Date());
    }
    
    //reissue the new token 
    public static String reissueIfValid(String token) {
        Claims c = parseClaims(token); // throws if invalid/expired
        int userId = Integer.parseInt(c.getSubject());
        String email = c.get("email", String.class);
        return generateToken(userId, email);
    }
    
    //compute the time of an expiry
    public static long secondsUntilExpiry(String token) {
        Claims c = parseClaims(token);
        long now = System.currentTimeMillis();
        return Math.max(0, (c.getExpiration().getTime() - now) / 1000);
    }

    
    //check the expiry of the token against threshold 
    public static boolean isExpiringSoon(String token, long thresholdSeconds) {
        return secondsUntilExpiry(token) <= thresholdSeconds;
 
    }
    
    //get the token jti 
    public static String getJti(String token) {
        Claims c = parseClaims(token);     // throws if invalid/expired
        return c.getId();                  // JJWT maps jti -> Claims.getId()
    }

    /** Returns the expiration time in each millis from a signed, unexpired JWT. */
    public static long getExpirationMillis(String token) {
        Claims c = parseClaims(token);     // throws if invalid/expired
        return c.getExpiration().getTime();
    }
}
