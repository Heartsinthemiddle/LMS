package com.lms.repository;

import com.lms.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SubscriptionPlan entity operations.
 * Provides CRUD operations and custom queries for subscription plan management.
 */
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    /**
     * Find a subscription plan by name
     * 
     * @param name the plan name
     * @return Optional containing the plan if found
     */
    Optional<SubscriptionPlan> findByName(String name);

    /**
     * Find all non-deleted subscription plans
     * 
     * @return list of non-deleted plans
     */
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.isDeleted = false")
    List<SubscriptionPlan> findAllActive();

    /**
     * Find all active subscription plans
     * 
     * @return list of plans that are marked as active
     */
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.isDeleted = false")
    List<SubscriptionPlan> findByIsDeletedFalse();

    /**
     * Check if a subscription plan exists by name
     * 
     * @param name the plan name
     * @return true if plan exists
     */
    boolean existsByName(String name);

    /**
     * Search subscription plans by name or description
     * 
     * @param keyword the search keyword
     * @return list of matching plans
     */
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE LOWER(sp.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(sp.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND sp.isDeleted = false")
    List<SubscriptionPlan> searchPlans(@Param("keyword") String keyword);
}

