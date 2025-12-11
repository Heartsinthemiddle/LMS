package com.lms.service;

import com.lms.entity.Roles;
import com.lms.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for role management operations.
 * Handles CRUD operations and business logic for roles.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    /**
     * Get all roles
     * 
     * @return list of all roles
     */
    @Transactional(readOnly = true)
    public List<Roles> getAllRoles() {
        log.debug("Fetching all roles");
        return roleRepository.findAll();
    }

    /**
     * Get role by ID
     * 
     * @param id the role ID
     * @return Optional containing the role if found
     */
    @Transactional(readOnly = true)
    public Optional<Roles> getRoleById(Long id) {
        log.debug("Fetching role by ID: {}", id);
        return roleRepository.findById(id);
    }

    /**
     * Get role by name
     * 
     * @param name the role name
     * @return Optional containing the role if found
     */
    @Transactional(readOnly = true)
    public Optional<Roles> getRoleByName(String name) {
        log.debug("Fetching role by name: {}", name);
        return roleRepository.findByRole(name);
    }

    /**
     * Check if role exists by name
     * 
     * @param name the role name
     * @return true if role exists
     */
    @Transactional(readOnly = true)
    public boolean roleExists(String name) {
        log.debug("Checking if role exists: {}", name);
        return roleRepository.existsByRole(name);
    }

    /**
     * Create a new role
     * 
     * @param role the role to create
     * @return the created role
     */
    @Transactional
    public Roles createRole(Roles role) {
        log.info("Creating new role: {}", role.getRole().name());
        
        if (roleRepository.existsByRole(role.getRole().name())) {
            throw new IllegalArgumentException("Role already exists: " + role.getRole().name());
        }
        
        return roleRepository.save(role);
    }

    /**
     * Update an existing role
     * 
     * @param id the role ID to update
     * @param role the updated role data
     * @return the updated role
     */
    @Transactional
    public Roles updateRole(Long id, Roles role) {
        log.info("Updating role: {}", id);
        
        Roles existingRole = roleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Role not found: {}", id);
                    return new IllegalArgumentException("Role not found: " + id);
                });
        
        existingRole.setRole(role.getRole());
        existingRole.setDescription(role.getDescription());
        
        return roleRepository.save(existingRole);
    }

    /**
     * Delete a role by ID
     * 
     * @param id the role ID to delete
     */
    @Transactional
    public void deleteRole(Long id) {
        log.info("Deleting role: {}", id);
        
        if (!roleRepository.existsById(id)) {
            log.warn("Role not found for deletion: {}", id);
            throw new IllegalArgumentException("Role not found: " + id);
        }
        
        roleRepository.deleteById(id);
        log.debug("Role deleted successfully: {}", id);
    }
}

