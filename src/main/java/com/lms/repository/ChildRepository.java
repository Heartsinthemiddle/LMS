package com.lms.repository;

import com.lms.entity.Child;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChildRepository extends JpaRepository<Child,Long> {
    Optional<Child> findByExternalChildId(Long externalId);

    List<Child> findByParentId(Long id);
}
