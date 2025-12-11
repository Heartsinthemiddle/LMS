package com.lms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Base entity class providing common fields for all entities.
 * This class includes audit fields (createdAt, updatedAt, createdBy, updatedBy, deletedBy, deletedAt)
 * and a soft delete flag. Other entity classes should extend this class to inherit these common fields.
 */
@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity {

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    protected String createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    protected LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    protected String updatedBy;

    @Column(name = "deleted_at")
    protected LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 100)
    protected String deletedBy;

    @Column(name = "is_deleted", nullable = false)
    protected Boolean isDeleted = false;
}
