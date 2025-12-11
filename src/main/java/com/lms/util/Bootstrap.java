package com.lms.util;

import com.lms.entity.Permissions;
import com.lms.entity.Role;
import com.lms.entity.Roles;
import com.lms.entity.User;
import com.lms.repository.PermissionsRepository;
import com.lms.repository.RoleRepository;
import com.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class Bootstrap implements CommandLineRunner {
    private final PermissionsRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        log.info("---- Starting LMS Bootstrap Initialization ----");

        // 1️⃣ Create Default Permissions
        Permissions userRead = createPermission("USER_READ", "Read user details");
        Permissions userWrite = createPermission("USER_WRITE", "Modify user details");
        Permissions roleRead = createPermission("ROLE_READ", "Read roles");
        Permissions roleWrite = createPermission("ROLE_WRITE", "Modify roles");

        Set<Permissions> allPermissions = Set.of(userRead, userWrite, roleRead, roleWrite);

        // 2️⃣ Create Roles and Assign Permissions
        Roles superAdminRole = createRole(Role.ADMIN, "Full system access", allPermissions);
        Roles adminRole = createRole(Role.ADMIN, "Admin level access", Set.of(userRead, userWrite, roleRead));

        // 3️⃣ Create Default Super Admin User
        createSuperAdmin(superAdminRole);
        saveRoles("Full system access", allPermissions);

        log.info("---- LMS Bootstrap Initialization Completed ----");
    }

    // ----------------------
    // Helper Methods
    // ----------------------

    private Permissions createPermission(String name, String description) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> {
                    Permissions perm = Permissions.builder()
                            .name(name)
                            .description(description)
                            .build();
                    permissionRepository.save(perm);
                    log.info("Created permission: {}", name);
                    return perm;
                });
    }

    private Roles createRole(Role roleEnum, String description, Set<Permissions> permissions) {
        return roleRepository.findByRole(roleEnum)
                .orElseGet(() -> {
                    Roles role = Roles.builder()
                            .role(roleEnum)
                            .description(description)
                            .permissions(permissions)
                            .build();
                    roleRepository.save(role);
                    log.info("Created role: {}", roleEnum.name());
                    return role;
                });
    }

    private void createSuperAdmin(Roles superAdminRole) {

        String email = "superadmin@yopmail.com";

        if (userRepository.findByEmail(email).isPresent()) {
            log.info("Super Admin already exists, skipping creation.");
            return;
        }

        User superAdmin = User.builder()
                .username("superadmin")
                .email(email)
                .password(passwordEncoder.encode("Admin@123")) // default password
                .roles(superAdminRole)
                .isActive(true)
                .build();

        userRepository.save(superAdmin);
        log.info("Super Admin user created successfully: {}", email);
    }

    private void saveRoles(String description, Set<Permissions> permissions) {
        List<Roles> rolesList1 = roleRepository.findAll();
        List<String> roleNames = rolesList1.stream()
                .map(role -> role.getRole().name())
                .toList();
        if (!rolesList1.isEmpty() && !roleNames.contains(Role.CHILD.name())
                && !roleNames.contains(Role.DECIDING_PARENT.name())
                && !roleNames.contains(Role.NON_DECIDING_PARENT.name())) {
            List<Roles> rolesList = List.of(
                    Roles.builder()
                            .role(Role.CHILD)
                            .description(description)
                            .permissions(permissions)
                            .build(),
                    Roles.builder()
                            .role(Role.DECIDING_PARENT)
                            .description(description)
                            .permissions(permissions)
                            .build(),
                    Roles.builder()
                            .role(Role.NON_DECIDING_PARENT)
                            .description(description)
                            .permissions(permissions)
                            .build()
            );
            roleRepository.saveAll(rolesList);

        }
    }


}
