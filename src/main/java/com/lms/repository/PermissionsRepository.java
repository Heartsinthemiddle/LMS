package com.lms.repository;

import com.lms.entity.Permissions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionsRepository extends JpaRepository<Permissions,Long> {
    Optional<Permissions> findByName(String name);

}
