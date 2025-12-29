package com.lms.service;

import com.lms.dto.request.CreateUserSubscriptionRequest;
import com.lms.dto.request.UpdateUserSubscriptionRequest;
import com.lms.dto.response.UserSubscriptionResponse;
import com.lms.entity.*;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for user subscription management operations.
 * Handles CRUD operations and business logic for user subscriptions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserSubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final CoursePackageRepository coursePackageRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ParentRepository parentRepository;
    private final RusticiClient rusticiClient;
    private final ChildRepository childRepository;

    /**
     * Get the current authenticated user's username
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "SYSTEM";
    }

    /**
     * Create a new user subscription
     * 
     * @param request the create user subscription request
     * @return the created user subscription response
     */
    @Transactional
    public UserSubscriptionResponse createUserSubscription(CreateUserSubscriptionRequest request) {

        // 1️⃣ Validate user
        Parent parent = parentRepository.findByExternalParentId(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));

        User user = userRepository.findByParentId(parent.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2️⃣ Validate plan
        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        // 3️⃣ Create subscription
        UserSubscription subscription = new UserSubscription();
        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setStartDate(request.getStartDate());
        subscription.setEndDate(request.getEndDate());
        subscription.setStripeCustomerId(request.getStripeCustomerId());
        subscription.setStripeSubscriptionId(request.getStripeSubscriptionId());
        subscription.setStatus("ACTIVE");
        subscription.setIsDeleted(false);

        userSubscriptionRepository.save(subscription);

        // 4️⃣ Create enrollments ONLY (NO SCORM)
        if (plan.getCoursePackage() != null) {

            List<Course> courses =
                    courseRepository.findByCoursePackageIdAndIsDeletedFalse(
                            plan.getCoursePackage().getId()
                    );

            for (Course course : courses) {

                if (course.getCourseType() == CourseType.PARENT) {

                    // Parent enrollment
                    Enrollment enrollment = new Enrollment();
                    enrollment.setCourse(course);
                    enrollment.setParent(parent);
                    enrollment.setLearnerType(LearnerType.PARENT_LEARNER);
                    enrollment.setProgressPercentage(0);
                    enrollment.setIsCompleted(false);

                    enrollmentRepository.save(enrollment);

                } else if (course.getCourseType() == CourseType.CHILD) {

                    // Create enrollment for EACH child
                    List<Child> children = childRepository.findByParentId(parent.getId());

                    for (Child child : children) {

                        Enrollment enrollment = new Enrollment();
                        enrollment.setCourse(course);
                        enrollment.setParent(parent);
                        enrollment.setChild(child);
                        enrollment.setLearnerType(LearnerType.CHILD_LEARNER);
                        enrollment.setProgressPercentage(0);
                        enrollment.setIsCompleted(false);

                        enrollmentRepository.save(enrollment);
                    }
                }

            }
        }

        return mapToResponse(subscription);
    }



    /**
     * Get all non-deleted user subscriptions
     * 
     * @return list of user subscription responses
     */
    @Transactional(readOnly = true)
    public List<UserSubscriptionResponse> getAllUserSubscriptions() {
        log.debug("Fetching all non-deleted user subscriptions");
        List<UserSubscription> subscriptions = userSubscriptionRepository.findAllActive();
        return subscriptions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a user subscription by ID
     * 
     * @param id the user subscription ID
     * @return the user subscription response
     * @throws ResourceNotFoundException if subscription not found or is deleted
     */
    @Transactional(readOnly = true)
    public UserSubscriptionResponse getUserSubscriptionById(Long id) {
        log.debug("Fetching user subscription by ID: {}", id);
        UserSubscription subscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User subscription not found with ID: {}", id);
                    return new ResourceNotFoundException("User subscription not found with ID: " + id);
                });

        if (Boolean.TRUE.equals(subscription.getIsDeleted())) {
            throw new ResourceNotFoundException("User subscription not found with ID: " + id);
        }

        return mapToResponse(subscription);
    }

    /**
     * Get subscription by user ID
     * 
     * @param userId the user ID
     * @return the user subscription response
     * @throws ResourceNotFoundException if no subscription found
     */
    @Transactional(readOnly = true)
    public UserSubscriptionResponse getUserSubscriptionByUserId(Long userId) {
        log.debug("Fetching user subscription for user ID: {}", userId);
        UserSubscription subscription = userSubscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("User subscription not found for user ID: {}", userId);
                    return new ResourceNotFoundException("User subscription not found for user ID: " + userId);
                });

        if (Boolean.TRUE.equals(subscription.getIsDeleted())) {
            throw new ResourceNotFoundException("User subscription not found for user ID: " + userId);
        }

        return mapToResponse(subscription);
    }

    /**
     * Get all subscriptions by status
     * 
     * @param status the subscription status
     * @return list of user subscription responses with the given status
     */
    @Transactional(readOnly = true)
    public List<UserSubscriptionResponse> getSubscriptionsByStatus(String status) {
        log.debug("Fetching subscriptions by status: {}", status);
        List<UserSubscription> subscriptions = userSubscriptionRepository.findByStatus(status);
        return subscriptions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all subscriptions expiring before a date
     * 
     * @param endDate the end date to check
     * @return list of expiring user subscriptions
     */
    @Transactional(readOnly = true)
    public List<UserSubscriptionResponse> getExpiringSubscriptions(LocalDate endDate) {
        log.debug("Fetching subscriptions expiring before: {}", endDate);
        List<UserSubscription> subscriptions = userSubscriptionRepository.findExpiringSubscriptions(endDate);
        return subscriptions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get subscriptions for a specific plan
     * 
     * @param planId the plan ID
     * @return list of subscriptions for the plan
     */
    @Transactional(readOnly = true)
    public List<UserSubscriptionResponse> getSubscriptionsByPlan(Long planId) {
        log.debug("Fetching subscriptions for plan ID: {}", planId);
        List<UserSubscription> subscriptions = userSubscriptionRepository.findByPlanId(planId);
        return subscriptions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Check if user has active subscription
     * 
     * @param userId the user ID
     * @return true if user has active subscription
     */
    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(Long userId) {
        log.debug("Checking if user has active subscription: {}", userId);
        return userSubscriptionRepository.hasActiveSubscription(userId);
    }

    /**
     * Update an existing user subscription
     * 
     * @param id the user subscription ID to update
     * @param request the update user subscription request
     * @return the updated user subscription response
     * @throws ResourceNotFoundException if subscription not found
     */
    @Transactional
    public UserSubscriptionResponse updateUserSubscription(Long id, UpdateUserSubscriptionRequest request) {
        log.info("Updating user subscription with ID: {}", id);

        UserSubscription subscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User subscription not found with ID: {}", id);
                    return new ResourceNotFoundException("User subscription not found with ID: " + id);
                });

        if (Boolean.TRUE.equals(subscription.getIsDeleted())) {
            throw new ResourceNotFoundException("User subscription not found with ID: " + id);
        }

        // Update plan if provided
        if (request.getPlanId() != null) {
            SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with ID: " + request.getPlanId()));

            if (Boolean.TRUE.equals(plan.getIsDeleted())) {
                throw new IllegalArgumentException("Selected subscription plan is deleted");
            }

            subscription.setPlan(plan);
        }

        // Update dates if provided
        if (request.getStartDate() != null) {
            subscription.setStartDate(request.getStartDate());
        }

        if (request.getEndDate() != null) {
            subscription.setEndDate(request.getEndDate());
        }

        // Validate dates
        if (subscription.getEndDate() != null && subscription.getStartDate() != null &&
                subscription.getEndDate().isBefore(subscription.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        // Update Stripe IDs if provided
        if (request.getStripeCustomerId() != null) {
            subscription.setStripeCustomerId(request.getStripeCustomerId());
        }

        if (request.getStripeSubscriptionId() != null) {
            subscription.setStripeSubscriptionId(request.getStripeSubscriptionId());
        }

        // Update status if provided
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            subscription.setStatus(request.getStatus());
        }

        subscription.setUpdatedBy(getCurrentUsername());

        UserSubscription updatedSubscription = userSubscriptionRepository.save(subscription);
        log.info("User subscription updated successfully with ID: {}", id);

        return mapToResponse(updatedSubscription);
    }

    /**
     * Soft delete a user subscription (marks as deleted without removing from database)
     * 
     * @param id the user subscription ID to delete
     * @throws ResourceNotFoundException if subscription not found
     */
    @Transactional
    public void deleteUserSubscription(Long id) {
        log.info("Deleting user subscription with ID: {}", id);

        UserSubscription subscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User subscription not found with ID: {}", id);
                    return new ResourceNotFoundException("User subscription not found with ID: " + id);
                });

        if (Boolean.TRUE.equals(subscription.getIsDeleted())) {
            throw new ResourceNotFoundException("User subscription not found with ID: " + id);
        }

        subscription.setIsDeleted(true);
        subscription.setDeletedAt(LocalDateTime.now());
        subscription.setDeletedBy(getCurrentUsername());
        subscription.setStatus("CANCELLED");

        userSubscriptionRepository.save(subscription);
        log.info("User subscription soft deleted successfully with ID: {}", id);
    }

    /**
     * Restore a soft-deleted user subscription
     * 
     * @param id the user subscription ID to restore
     * @return the restored user subscription response
     * @throws ResourceNotFoundException if subscription not found
     */
    @Transactional
    public UserSubscriptionResponse restoreUserSubscription(Long id) {
        log.info("Restoring user subscription with ID: {}", id);

        UserSubscription subscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User subscription not found with ID: {}", id);
                    return new ResourceNotFoundException("User subscription not found with ID: " + id);
                });

        if (!Boolean.TRUE.equals(subscription.getIsDeleted())) {
            throw new IllegalArgumentException("User subscription is not deleted");
        }

        subscription.setIsDeleted(false);
        subscription.setDeletedAt(null);
        subscription.setDeletedBy(null);
        subscription.setStatus("ACTIVE");
        subscription.setUpdatedBy(getCurrentUsername());

        UserSubscription restoredSubscription = userSubscriptionRepository.save(subscription);
        log.info("User subscription restored successfully with ID: {}", id);

        return mapToResponse(restoredSubscription);
    }

    /**
     * Convert UserSubscription entity to UserSubscriptionResponse DTO
     * 
     * @param subscription the user subscription entity
     * @return the user subscription response DTO
     */
    private UserSubscriptionResponse mapToResponse(UserSubscription subscription) {
        return UserSubscriptionResponse.builder()
                .id(subscription.getId())
                .userId(subscription.getUser() != null ? subscription.getUser().getId() : null)
                .userName(subscription.getUser() != null ? subscription.getUser().getUsername() : null)
                .userEmail(subscription.getUser() != null ? subscription.getUser().getEmail() : null)
                .planId(subscription.getPlan() != null ? subscription.getPlan().getId() : null)
                .planName(subscription.getPlan() != null ? subscription.getPlan().getName() : null)
                .monthlyPrice(subscription.getPlan() != null ? subscription.getPlan().getMonthlyPrice() : null)
                .yearlyPrice(subscription.getPlan() != null ? subscription.getPlan().getYearlyPrice() : null)
                .stripeCustomerId(subscription.getStripeCustomerId())
                .stripeSubscriptionId(subscription.getStripeSubscriptionId())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .status(subscription.getStatus())
                .createdAt(subscription.getCreatedAt())
                .createdBy(subscription.getCreatedBy())
                .updatedAt(subscription.getUpdatedAt())
                .updatedBy(subscription.getUpdatedBy())
                .deletedAt(subscription.getDeletedAt())
                .deletedBy(subscription.getDeletedBy())
                .isDeleted(subscription.getIsDeleted())
                .build();
    }
}

