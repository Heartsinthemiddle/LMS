package com.lms.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for JWT token operations.
 * Provides helper methods to extract and generate JWTs.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    // Token expiration: 10 hours (example)
    private static final long JWT_EXPIRATION = 10 * 60 * 60 * 1000;

    /**
     * Generate a JWT token using username.
     *
     * @param username the username to set in token's subject
     * @return generated JWT token
     */
    public  String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    public  String generateToken(String username,Map<String,Object> extraClaims) {
//        Map<String, Object> claims = new HashMap<>();
        return createToken(extraClaims, username);
    }

    /**
     * Create JWT token with claims + subject.
     */
    private  String createToken(Map<String, Object> claims, String subject) {

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(now))
                .expiration(new Date(now + JWT_EXPIRATION))
                .signWith(getSigningKey()) // automatically uses HS256
                .compact();
    }

    /**
     * Extract username from token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract expiration date from token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract a specific claim from token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get signing key from secret.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isValidInternalToken(String token) {
        try {
            // 1. Parse and validate signature
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 2. Validate expiration
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                return false; // token expired
            }

            // 3. Validate username (subject)
            String username = claims.getSubject();
            if (username == null || username.trim().isEmpty()) {
                return false; // invalid token
            }
            if(!username.equals("superadmin")){
                return false; // reserved for external tokens
            }

            return true; // token is valid internal JWT

        } catch (Exception ex) {
            // Any signature / parsing error → invalid token
            return false;
        }
    }

}
