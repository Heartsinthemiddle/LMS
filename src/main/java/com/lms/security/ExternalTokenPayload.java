package com.lms.security;

import com.lms.entity.Role;
import io.jsonwebtoken.Claims;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents the data extracted from a verified external token.
 */
@Getter
@Builder
public class ExternalTokenPayload {
    private final Long userId;
    private final String username;
    private final Role role;
    private final String email;
    private final List<String> permissions;
    private final Map<String, Object> claims;
    private final String caseNumber;
    private final String gender;
    private final Map<String,Object> child;
    private final Map<String,Object> parent;


    public List<String> getPermissions() {
        return permissions != null ? permissions : Collections.emptyList();
    }
}

