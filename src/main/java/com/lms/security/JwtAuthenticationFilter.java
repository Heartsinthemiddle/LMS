package com.lms.security;

import com.lms.entity.*;
import com.lms.repository.ChildRepository;
import com.lms.repository.ParentRepository;
import com.lms.repository.RoleRepository;
import com.lms.repository.UserRepository;
import com.lms.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER = "Bearer ";

    private final JwtUtil jwtService;  // INTERNAL JWT Validator
    private final ExternalTokenVerifier tokenVerifier; // EXTERNAL TOKEN Validator

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ChildRepository childRepository;
    private final ParentRepository parentRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {

            try {
                // ⭐ STEP 1 — CHECK IF TOKEN IS INTERNAL LMS TOKEN
                if (jwtService.isValidInternalToken(token)) {
                    handleInternalToken(token, request);
                    filterChain.doFilter(request, response);
                    return;
                }

                // ⭐ STEP 2 — OTHERWISE, TREAT AS EXTERNAL TOKEN
                handleExternalToken(token, request);

            } catch (TokenValidationException ex) {
                logger.warn("Token validation failed: {}", ex.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"" + ex.getMessage() + "\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    // ------------------------------------------------------------
    // INTERNAL LOGIN TOKEN (SUPER ADMIN)
    // ------------------------------------------------------------
    private void handleInternalToken(String token, HttpServletRequest request) {

        String username = jwtService.extractUsername(token);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found for internal token"));

        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + user.getRoles().getRole().name());

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(authority)
                );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        logger.debug("Authenticated internal token user: {}, roles={}", username, authentication.getAuthorities());
    }


    // ------------------------------------------------------------
    // EXTERNAL TOKEN (CHILD, PARENT, ETC.)
    // ------------------------------------------------------------
    private void handleExternalToken(String token, HttpServletRequest request)
            throws TokenValidationException, RemoteException {

        ExternalTokenPayload payload = tokenVerifier.verify(token);

        logger.debug("Valid external JWT for user: {}", payload.getUsername());

        Optional<User> userOpt = userRepository.findByUsername(payload.getUsername());

        User user = userOpt.orElseGet(() -> createExternalUser(payload));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRoles().getRole().name()))
                );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        logger.debug("Authenticated external user: {}", payload.getUsername());
    }

    // ------------------------------------------------------------
    // CREATE USER FOR EXTERNAL TOKEN
    // ------------------------------------------------------------
    private User createExternalUser(ExternalTokenPayload payload) {

        Roles role = roleRepository.findByRole(payload.getRole())
                .orElseThrow(() -> new RuntimeException("Role not found: " + payload.getRole().name()));



        switch (role.getRole()) {

            // ==============================================================
            // ===================== CHILD LOGIN ============================
            // ==============================================================

            case CHILD -> {

                // USER we will return

                Map<String, Object> childMap = payload.getChild();
                Map<String, Object> parentMap = payload.getParent();

                // 1️⃣ Check if CHILD already exists
                Child child = childRepository
                        .findByExternalChildId(Long.valueOf(childMap.get("id").toString()))
                        .orElse(new Child());

                // Always update in case external provider changed details
                child.setExternalChildId(Long.valueOf(childMap.get("id").toString()));
                child.setName((String) childMap.get("name"));
                child.setUserName((String) childMap.get("userName"));
                child.setCaseNumber((String) childMap.get("caseNumber"));
                child.setGender((String) childMap.get("gender"));

                // -------------------------------
                // 2️⃣ Handle Parent
                // -------------------------------

                Long parentExternalId = Long.valueOf(parentMap.get("id").toString());
                String parentUserName = (String) parentMap.get("userName");

                // First check if parent *user* exists
                User parentUser = userRepository.findByUsername(parentUserName).orElse(null);

                Parent parent;

                if (parentUser != null) {
                    // Parent user exists → reuse parent
                    parent = parentUser.getParent();
                } else {
                    // Parent user does NOT exist → create parent + user
                    parent = parentRepository
                            .findByExternalParentId(parentExternalId)
                            .orElse(new Parent());

                    parent.setExternalParentId(parentExternalId);
                    parent.setName((String) parentMap.get("name"));
                    parent.setUserName(parentUserName);
                    parent.setUserEmail((String) parentMap.get("email"));
                    parent.setGender((String) parentMap.get("gender"));
                    String parentTypeStr = (String) parentMap.get("type");
                    if ("NON_DECIDING".equalsIgnoreCase(parentTypeStr)) {
                        parent.setParentType(ParentType.NON_DECIDING);
                    } else {
                        parent.setParentType(
                                ParentType.DECIDING
                        );
                    }

                    parentRepository.save(parent);

                    // Create Parent User
                    Roles parentRole = roleRepository
                            .findByRole(Role.DECIDING_PARENT)
                            .orElseThrow();

                    parentUser = new User();
                    parentUser.setUsername(parentUserName);
                    parentUser.setEmail((String) parentMap.get("email"));
                    parentUser.setRoles(parentRole);
                    parentUser.setParent(parent);

                    userRepository.save(parentUser);
                }


                // 3️⃣ Link Parent → Child
                child.setParent(parent);
                childRepository.save(child);
                User childUser = new User();
                childUser.setUsername(childMap.get("userName").toString());
                childUser.setEmail(null);
                childUser.setRoles(role);

                // 4️⃣ Link Child → Logging User
                childUser.setChild(child);
                userRepository.save(childUser);
                return childUser;
            }

            // ==============================================================
            // ===================== PARENT LOGIN ===========================
            // ==============================================================

            case DECIDING_PARENT, NON_DECIDING_PARENT -> {

                Map<String, Object> parentMap = payload.getParent();
                Map<String, Object> childMap  = payload.getChild();

                String parentUserName = (String) parentMap.get("userName");

                // =========================================================
                // 1️⃣ Check if PARENT USER already exists
                // =========================================================
                User parentUser = userRepository.findByUsername(parentUserName).orElse(null);

                Parent parent;

                if (parentUser != null && parentUser.getParent() != null) {
                    // Reuse existing parent entity
                    parent = parentUser.getParent();
                } else {

                    // =========================================================
                    // 2️⃣ CREATE OR REUSE Parent ENTITY
                    // =========================================================
                    parent = parentRepository
                            .findByExternalParentId(Long.valueOf(parentMap.get("id").toString()))
                            .orElse(new Parent());

                    parent.setExternalParentId(Long.valueOf(parentMap.get("id").toString()));
                    parent.setName((String) parentMap.get("name"));
                    parent.setUserName(parentUserName);
                    parent.setUserEmail((String) parentMap.get("email"));
                    parent.setGender((String) parentMap.get("gender"));
                    parent.setParentType(
                            role.getRole() == Role.DECIDING_PARENT
                                    ? ParentType.DECIDING
                                    : ParentType.NON_DECIDING
                    );

                    parentRepository.save(parent);

                    // =========================================================
                    // 3️⃣ CREATE PARENT USER IF MISSING
                    // =========================================================
                    if (parentUser == null) {

                        Roles parentRole = roleRepository.findByRole(role.getRole())
                                .orElseThrow(() -> new RuntimeException("Parent role missing"));

                        parentUser = new User();
                        parentUser.setUsername(parentUserName);
                        parentUser.setEmail((String) parentMap.get("email"));
                        parentUser.setRoles(parentRole);
                        parentUser.setParent(parent);

                        userRepository.save(parentUser);
                    }
                }

                // =========================================================
                // 4️⃣ HANDLE CHILD IF CLAIMS INCLUDE CHILD DATA
                // =========================================================
                if (childMap != null && !childMap.isEmpty()) {

                    Long childExternalId = Long.valueOf(childMap.get("id").toString());

                    Child child = childRepository.findByExternalChildId(childExternalId)
                            .orElse(new Child());

                    child.setExternalChildId(childExternalId);
                    child.setName((String) childMap.get("name"));
                    child.setUserName((String) childMap.get("userName"));
                    child.setCaseNumber((String) childMap.get("caseNumber"));
                    child.setGender((String) childMap.get("gender"));

                    // link parent → child
                    child.setParent(parent);
                    childRepository.save(child);

                    // =====================================================
                    // 5️⃣ Ensure CHILD USER exists
                    // =====================================================
                    User childUser = userRepository.findByUsername(child.getUserName()).orElse(null);

                    if (childUser == null) {
                        Roles childRole = roleRepository.findByRole(Role.CHILD)
                                .orElseThrow(() -> new RuntimeException("CHILD role missing"));

                        childUser = new User();
                        childUser.setUsername(child.getUserName());
                        childUser.setEmail(null); // external may not provide
                        childUser.setRoles(childRole);
                        childUser.setChild(child);

                        userRepository.save(childUser);
                    }
                }

                // =========================================================
                // 6️⃣ SET parent for the LOGGED-IN user instance
                // =========================================================
               return parentUser;
            }



            default -> {
                return null;
            }
        }

    }




    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (!StringUtils.hasText(header)) {
            header = request.getParameter("token"); // fallback for Swagger testing
        }
        if (StringUtils.hasText(header) && header.startsWith(BEARER)) {
            return header.substring(BEARER.length());
        }
        return null;
    }
}
