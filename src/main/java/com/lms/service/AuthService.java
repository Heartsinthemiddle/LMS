package com.lms.service;

import com.lms.dto.request.LoginRequest;
import com.lms.dto.request.RegisterRequest;
import com.lms.dto.response.AuthResponse;
import com.lms.entity.Role;
import com.lms.entity.Roles;
import com.lms.entity.User;
import com.lms.repository.RoleRepository;
import com.lms.repository.UserRepository;
import com.lms.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for authentication operations.
 * Handles user registration and login functionality.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RoleRepository roleRepository;


    /**
     * Register a new user.
     * 
     * @param request the registration request
     * @return AuthResponse with token and user details
     * @throws IllegalArgumentException if username or email already exists
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        logger.info("Attempting to register new user with username: {}", request.getUsername());

        // Check if username already exists
        if (userService.existsByUsername(request.getUsername())) {
            logger.warn("Registration failed: Username already exists - {}", request.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userService.existsByEmail(request.getEmail())) {
            logger.warn("Registration failed: Email already exists - {}", request.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        // Validate role
        if (request.getRole() == null) {
            logger.warn("Registration failed: Role is required");
            throw new IllegalArgumentException("Role is required");
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
//                .role(request.getRole())
                .isActive(true)
                .build();
        Map<String, Object> childMap = new HashMap<>();
        childMap.put("id", 12345L);
        childMap.put("externalId", 99999L);
        childMap.put("name", "Abhi Kumar");
        childMap.put("userName", "abhi123");
        childMap.put("caseNumber", "45632");
        childMap.put("gender", "M");

        Map<String, Object> parentMap = new HashMap<>();
        parentMap.put("id", 56789L);
        parentMap.put("externalId", 88888L);
        parentMap.put("name", "John Kumar");
        parentMap.put("userName", "johnParent");
        parentMap.put("email", "john.parent@yopmail.com");
        parentMap.put("gender", "M");
        parentMap.put("type", "FATHER");
        parentMap.put("role",Role.DECIDING_PARENT);// hardcoded ParentType

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", Role.CHILD);
        claims.put("child", childMap);
        claims.put("parent", parentMap);

        String token =jwtUtil.generateToken(request.getUsername(),claims);
        Optional<Roles> roles = roleRepository.findByRole(Role.CHILD);
        user.setRoles(roles.get());

        user = userRepository.save(user);
        logger.info("User registered successfully with ID: {}", user.getId());

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
//                .role(user.getRole())
                .token(token)
                .build();
    }

    /**
     * Authenticate user and generate JWT token.
     * 
     * @param request the login request
     * @return AuthResponse with token and user details
     * @throws BadCredentialsException if credentials are invalid
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        logger.info("Attempting to login user: {}", request.getUsernameOrEmail());

        try {
            // Authenticate user
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );

            // Extract authenticated user details

            Optional<User> user = userRepository.findByUsername(request.getUsernameOrEmail());
            if (user.isEmpty()) {
                user = userRepository.findByEmail(request.getUsernameOrEmail());
            }
            if (!user.isPresent()){
                logger.warn("Login failed: User not found - {}", request.getUsernameOrEmail());
                throw new BadCredentialsException("Invalid username/email or password");
            }

            // Generate JWT token
            String jwtToken = jwtUtil.generateToken(request.getUsernameOrEmail());

            logger.info("User logged in successfully: {}", request.getUsernameOrEmail());

            return AuthResponse.builder()
                    .id(user.get().getId())
                    .username(user.get().getUsername())
                    .email(user.get().getEmail())

                    // adjust if user.getRoles() is a list
                    .role(user.get().getRoles().getRole())

                    .token(jwtToken)
                    .build();

        } catch (BadCredentialsException e) {
            logger.warn("Login failed: Invalid credentials for user: {}", request.getUsernameOrEmail());
            throw new BadCredentialsException("Invalid username/email or password");
        } catch (LockedException e) {
            logger.warn("User account is locked: {}", request.getUsernameOrEmail());
            throw new LockedException("Your account is locked");
        } catch (DisabledException e) {
            logger.warn("User account is disabled: {}", request.getUsernameOrEmail());
            throw new DisabledException("Your account is disabled");
        } catch (CredentialsExpiredException e) {
            logger.warn("Credentials expired for user: {}", request.getUsernameOrEmail());
            throw new CredentialsExpiredException("Your password has expired");
        }
    }

}

