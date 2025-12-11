package com.lms.security;

import com.lms.entity.Role;
import com.lms.entity.Roles;
import com.lms.repository.RoleRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

/**
 * Verifies externally issued JWTs using the shared HMAC secret.
 * Extracts user id, username, roles, permissions and raw claims.
 */
@Component
public class ExternalTokenVerifier {

    private final String secret;
    private final RoleRepository roleRepository;

    public ExternalTokenVerifier(@Value("${jwt.secret}") String secret, RoleRepository roleRepository) {
        this.secret = secret;
        this.roleRepository = roleRepository;
    }

    public ExternalTokenPayload verify(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            validateExpiration(claims);

            // Extract the role
            String roleStr = (String) claims.get("role");
            Roles roles = roleRepository.findByRole(Role.valueOf(roleStr))
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleStr));

            // Extract CHILD nested claims (if available)
            Map<String, Object> childMap = (Map<String, Object>) claims.get("child");

            // Extract PARENT nested claims (if available)
            Map<String, Object> parentMap = (Map<String, Object>) claims.get("parent");

            return ExternalTokenPayload.builder()
                    .userId(extractUserIdFromChildOrParent(childMap, parentMap))
                    .username(claims.getSubject())
                    .email(extractEmailFromChildOrParent(childMap, parentMap))
                    .role(roles.getRole())
                    .caseNumber(extractCaseNumber(childMap))
                    .gender(extractGender(childMap, parentMap))
                    .claims(claims)
                    .child(childMap)
                    .parent(parentMap)
                    .build();

        } catch (ExpiredJwtException ex) {
            throw new TokenValidationException("Token expired", ex);
        } catch (JwtException ex) {
            throw new TokenValidationException("Invalid token", ex);
        } catch (IllegalArgumentException ex) {
            throw new TokenValidationException("Token missing required claims", ex);
        }
    }


    private void validateExpiration(Claims claims) {
        Date expiration = claims.getExpiration();
        if (expiration != null && expiration.toInstant().isBefore(Instant.now())) {
            throw new TokenValidationException("Token expired");
        }
    }

    private Long extractUserIdFromChildOrParent(Map<String, Object> child, Map<String, Object> parent) {
        if (child != null && child.get("id") != null) {
            return Long.valueOf(child.get("id").toString());
        }
        if (parent != null && parent.get("id") != null) {
            return Long.valueOf(parent.get("id").toString());
        }
        throw new IllegalArgumentException("No child or parent ID found in token");
    }

    private String extractCaseNumber(Map<String, Object> child) {
        return child != null ? (String) child.get("caseNumber") : null;
    }

    private String extractGender(Map<String, Object> child, Map<String, Object> parent) {
        if (child != null && child.get("gender") != null) {
            return child.get("gender").toString();
        }
        if (parent != null && parent.get("gender") != null) {
            return parent.get("gender").toString();
        }
        return null;
    }

    private String extractEmailFromChildOrParent(Map<String, Object> child, Map<String, Object> parent) {
        if (parent != null && parent.get("email") != null) {
            return parent.get("email").toString();
        }
        return null; // child has no email
    }


    private Long extractUserId(Claims claims) {
        Object idClaim = claims.get("userId");
        if (idClaim instanceof Number number) {
            return number.longValue();
        }
        if (idClaim instanceof String str && !str.isBlank()) {
            return Long.parseLong(str);
        }
        return null;
    }

    private String extractRole(Claims claims) {
        Object roleClaim = claims.get("role");

        if (roleClaim instanceof String str && !str.isBlank()) {
            return str;
        }

        return null;  // or throw an exception based on your logic
    }


    private List<String> extractStringList(Object raw) {
        if (raw == null) {
            return Collections.emptyList();
        }
        List<String> values = new ArrayList<>();
        if (raw instanceof List<?> list) {
            for (Object item : list) {
                if (item != null) {
                    values.add(item.toString());
                }
            }
            return values;
        }
        if (raw instanceof String str) {
            for (String part : str.split(",")) {
                if (!part.isBlank()) {
                    values.add(part.trim());
                }
            }
        }
        return values;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

