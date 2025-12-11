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

        Roles role = roleRepository.findByRole(payload.getRole().name())
                .orElseThrow(() -> new RuntimeException("Role not found: " + payload.getRole().name()));

        User newUser = new User();
        newUser.setId(payload.getUserId());
        newUser.setUsername(payload.getUsername());
        newUser.setEmail(payload.getEmail());
        newUser.setRoles(role);

        Long externalId = payload.getUserId();

        switch (role.getRole()) {

            case CHILD -> {
                Child child = new Child();
                child.setExternalChildId(externalId);
                child.setName(payload.getUsername());
                child.setUserName(payload.getUsername());
                child.setCaseNumber(payload.getCaseNumber());
                child.setGender(payload.getGender());
                childRepository.save(child);
                newUser.setChild(child);
            }

            case DECIDING_PARENT, NON_DECIDING_PARENT -> {
                Parent parent = new Parent();
                parent.setExternalChildId(externalId);
                parent.setName(payload.getUsername());
                parent.setUserName(payload.getUsername());
                parent.setUserEmail(payload.getEmail());
                parent.setGender(payload.getGender());
                parent.setParentType(
                        role.getRole() == Role.DECIDING_PARENT
                                ? ParentType.DECIDING
                                : ParentType.NON_DECIDING
                );
                parentRepository.save(parent);
                newUser.setParent(parent);
            }

            default -> throw new RuntimeException("Unhandled external role: " + role.getRole().name());
        }

        return userRepository.save(newUser);
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
