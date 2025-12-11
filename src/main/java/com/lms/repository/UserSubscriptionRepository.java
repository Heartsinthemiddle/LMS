package com.lms.repository;

import com.lms.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for UserSubscription entity operations.
 * Provides CRUD operations and custom queries for user subscription management.
 */
@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    /**
     * Find subscription by user ID
     * 
     * @param userId the user ID
     * @return Optional containing the subscription if found
     */
    Optional<UserSubscription> findByUserId(Long userId);

    /**
     * Find all non-deleted subscriptions
     * 
     * @return list of non-deleted subscriptions
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.isDeleted = false")
    List<UserSubscription> findAllActive();

    /**
     * Find all subscriptions by status
     * 
     * @param status the subscription status
     * @return list of subscriptions with the given status
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.status = :status AND us.isDeleted = false")
    List<UserSubscription> findByStatus(@Param("status") String status);

    /**
     * Find all active subscriptions expiring before a date
     * 
     * @param endDate the end date to check
     * @return list of subscriptions expiring before the date
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.status = 'ACTIVE' AND us.endDate < :endDate AND us.isDeleted = false")
    List<UserSubscription> findExpiringSubscriptions(@Param("endDate") LocalDate endDate);

    /**
     * Find subscription by Stripe subscription ID
     * 
     * @param stripeSubscriptionId the Stripe subscription ID
     * @return Optional containing the subscription if found
     */
    Optional<UserSubscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    /**
     * Find all subscriptions by plan ID
     * 
     * @param planId the plan ID
     * @return list of subscriptions for the given plan
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.plan.id = :planId AND us.isDeleted = false")
    List<UserSubscription> findByPlanId(@Param("planId") Long planId);

    /**
     * Check if user has active subscription
     * 
     * @param userId the user ID
     * @return true if user has active subscription
     */
    @Query("SELECT CASE WHEN COUNT(us) > 0 THEN true ELSE false END FROM UserSubscription us " +
           "WHERE us.user.id = :userId AND us.status = 'ACTIVE' AND us.isDeleted = false")
    boolean hasActiveSubscription(@Param("userId") Long userId);
}

