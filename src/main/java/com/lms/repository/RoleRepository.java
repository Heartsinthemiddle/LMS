package com.lms.repository;

import com.lms.entity.Role;
import com.lms.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Role entity operations.
 * Provides CRUD operations and custom queries for Role management.
 */
@Repository
public interface RoleRepository extends JpaRepository<Roles, Long> {

    /**
     * Find a role by its name
     * 
     * @param name the role name
     * @return Optional containing the role if found
     */
    Optional<Roles> findByRole(String name);

    /**
     * Check if a role exists by name
     * 
     * @param name the role name
     * @return true if role exists, false otherwise
     */
    boolean existsByRole(String name);

    Optional<Roles> findByRole(Role role);

}

