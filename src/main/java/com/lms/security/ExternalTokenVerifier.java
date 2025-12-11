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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
            String role = extractStringList(claims.get("roles")).toString();
            Roles roles = null;
            if(Role.CHILD.name().equals(role)){
                roles =  roleRepository.findByRole(Role.CHILD).orElse(null);
            }else if(Role.DECIDING_PARENT.name().equals(role)) {
                roles = roleRepository.findByRole(Role.DECIDING_PARENT).orElse(null);
            }else if(Role.NON_DECIDING_PARENT.name().equals(role)) {
                roles = roleRepository.findByRole(Role.NON_DECIDING_PARENT).orElse(null);
            }


            return ExternalTokenPayload.builder()
                    .userId(extractUserId(claims))
                    .username(claims.getSubject())
                    .role(roles.getRole())
                    .claims(claims)
                    .caseNumber("ryt1234")
                    .build();
        } catch (ExpiredJwtException ex) {
            throw new TokenValidationException("Token expired", ex);
        } catch (JwtException ex) {
            throw new TokenValidationException("Invalid token", ex);
        } catch (IllegalArgumentException ex) {
            throw new TokenValidationException("Token is missing required claims", ex);
        }
    }

    private void validateExpiration(Claims claims) {
        Date expiration = claims.getExpiration();
        if (expiration != null && expiration.toInstant().isBefore(Instant.now())) {
            throw new TokenValidationException("Token expired");
        }
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

