package com.lms.service;

import com.lms.dto.request.CreateSubscriptionPlanRequest;
import com.lms.dto.request.UpdateSubscriptionPlanRequest;
import com.lms.dto.response.SubscriptionPlanResponse;
import com.lms.entity.CoursePackage;
import com.lms.entity.SubscriptionPlan;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.CoursePackageRepository;
import com.lms.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for subscription plan management operations.
 * Handles CRUD operations and business logic for subscription plans.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final CoursePackageRepository coursePackageRepository;

    /**
     * Get the current authenticated user's username
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "SYSTEM";
    }

    /**
     * Create a new subscription plan
     * 
     * @param request the create subscription plan request
     * @return the created subscription plan response
     */
    @Transactional
    public SubscriptionPlanResponse createSubscriptionPlan(CreateSubscriptionPlanRequest request) {
        log.info("Creating new subscription plan with name: {}", request.getName());

        // Check if plan with same name already exists
        if (subscriptionPlanRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Subscription plan with name '" + request.getName() + "' already exists");
        }

        // Verify course package exists
        CoursePackage coursePackage = coursePackageRepository.findById(request.getCoursePackageId())
                .orElseThrow(() -> new ResourceNotFoundException("Course package not found with ID: " + request.getCoursePackageId()));

        if (Boolean.TRUE.equals(coursePackage.getIsDeleted())) {
            throw new IllegalArgumentException("Selected course package is deleted");
        }

        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setMonthlyPrice(request.getMonthlyPrice());
        plan.setYearlyPrice(request.getYearlyPrice());
        plan.setUserLimit(request.getUserLimit());
        plan.setChildLimit(request.getChildLimit());
        plan.setCoursePackage(coursePackage);
        plan.setCreatedBy(getCurrentUsername());
        plan.setIsDeleted(false);

        SubscriptionPlan savedPlan = subscriptionPlanRepository.save(plan);
        log.info("Subscription plan created successfully with ID: {}", savedPlan.getId());

        return mapToResponse(savedPlan);
    }

    /**
     * Get all non-deleted subscription plans
     * 
     * @return list of subscription plan responses
     */
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getAllSubscriptionPlans() {
        log.debug("Fetching all non-deleted subscription plans");
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAllActive();
        return plans.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a subscription plan by ID
     * 
     * @param id the subscription plan ID
     * @return the subscription plan response
     * @throws ResourceNotFoundException if plan not found or is deleted
     */
    @Transactional(readOnly = true)
    public SubscriptionPlanResponse getSubscriptionPlanById(Long id) {
        log.debug("Fetching subscription plan by ID: {}", id);
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Subscription plan not found with ID: {}", id);
                    return new ResourceNotFoundException("Subscription plan not found with ID: " + id);
                });

        if (Boolean.TRUE.equals(plan.getIsDeleted())) {
            throw new ResourceNotFoundException("Subscription plan not found with ID: " + id);
        }

        return mapToResponse(plan);
    }

    /**
     * Search subscription plans by keyword
     * 
     * @param keyword the search keyword
     * @return list of matching subscription plan responses
     */
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> searchSubscriptionPlans(String keyword) {
        log.debug("Searching subscription plans with keyword: {}", keyword);
        List<SubscriptionPlan> plans = subscriptionPlanRepository.searchPlans(keyword);
        return plans.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing subscription plan
     * 
     * @param id the subscription plan ID to update
     * @param request the update subscription plan request
     * @return the updated subscription plan response
     * @throws ResourceNotFoundException if plan not found
     */
    @Transactional
    public SubscriptionPlanResponse updateSubscriptionPlan(Long id, UpdateSubscriptionPlanRequest request) {
        log.info("Updating subscription plan with ID: {}", id);

        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Subscription plan not found with ID: {}", id);
                    return new ResourceNotFoundException("Subscription plan not found with ID: " + id);
                });

        if (Boolean.TRUE.equals(plan.getIsDeleted())) {
            throw new ResourceNotFoundException("Subscription plan not found with ID: " + id);
        }

        // Update only provided fields
        if (request.getName() != null && !request.getName().isEmpty()) {
            // Check if new name is unique (excluding current plan)
            Optional<SubscriptionPlan> existingPlan = subscriptionPlanRepository.findByName(request.getName());
            if (existingPlan.isPresent() && !existingPlan.get().getId().equals(id)) {
                throw new IllegalArgumentException("Subscription plan with name '" + request.getName() + "' already exists");
            }
            plan.setName(request.getName());
        }

        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            plan.setDescription(request.getDescription());
        }

        if (request.getMonthlyPrice() != null) {
            plan.setMonthlyPrice(request.getMonthlyPrice());
        }

        if (request.getYearlyPrice() != null) {
            plan.setYearlyPrice(request.getYearlyPrice());
        }

        if (request.getUserLimit() != null) {
            plan.setUserLimit(request.getUserLimit());
        }

        if (request.getChildLimit() != null) {
            plan.setChildLimit(request.getChildLimit());
        }

        if (request.getCoursePackageId() != null) {
            CoursePackage coursePackage = coursePackageRepository.findById(request.getCoursePackageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course package not found with ID: " + request.getCoursePackageId()));

            if (Boolean.TRUE.equals(coursePackage.getIsDeleted())) {
                throw new IllegalArgumentException("Selected course package is deleted");
            }

            plan.setCoursePackage(coursePackage);
        }

        plan.setUpdatedBy(getCurrentUsername());

        SubscriptionPlan updatedPlan = subscriptionPlanRepository.save(plan);
        log.info("Subscription plan updated successfully with ID: {}", id);

        return mapToResponse(updatedPlan);
    }

    /**
     * Soft delete a subscription plan (marks as deleted without removing from database)
     * 
     * @param id the subscription plan ID to delete
     * @throws ResourceNotFoundException if plan not found
     */
    @Transactional
    public void deleteSubscriptionPlan(Long id) {
        log.info("Deleting subscription plan with ID: {}", id);

        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Subscription plan not found with ID: {}", id);
                    return new ResourceNotFoundException("Subscription plan not found with ID: " + id);
                });

        if (Boolean.TRUE.equals(plan.getIsDeleted())) {
            throw new ResourceNotFoundException("Subscription plan not found with ID: " + id);
        }

        plan.setIsDeleted(true);
        plan.setDeletedAt(LocalDateTime.now());
        plan.setDeletedBy(getCurrentUsername());

        subscriptionPlanRepository.save(plan);
        log.info("Subscription plan soft deleted successfully with ID: {}", id);
    }

    /**
     * Restore a soft-deleted subscription plan
     * 
     * @param id the subscription plan ID to restore
     * @return the restored subscription plan response
     * @throws ResourceNotFoundException if plan not found
     */
    @Transactional
    public SubscriptionPlanResponse restoreSubscriptionPlan(Long id) {
        log.info("Restoring subscription plan with ID: {}", id);

        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Subscription plan not found with ID: {}", id);
                    return new ResourceNotFoundException("Subscription plan not found with ID: " + id);
                });

        if (!Boolean.TRUE.equals(plan.getIsDeleted())) {
            throw new IllegalArgumentException("Subscription plan is not deleted");
        }

        plan.setIsDeleted(false);
        plan.setDeletedAt(null);
        plan.setDeletedBy(null);
        plan.setUpdatedBy(getCurrentUsername());

        SubscriptionPlan restoredPlan = subscriptionPlanRepository.save(plan);
        log.info("Subscription plan restored successfully with ID: {}", id);

        return mapToResponse(restoredPlan);
    }

    /**
     * Check if a subscription plan exists by name
     * 
     * @param name the subscription plan name
     * @return true if plan exists and is not deleted
     */
    @Transactional(readOnly = true)
    public boolean planExists(String name) {
        log.debug("Checking if subscription plan exists: {}", name);
        Optional<SubscriptionPlan> plan = subscriptionPlanRepository.findByName(name);
        return plan.isPresent() && !Boolean.TRUE.equals(plan.get().getIsDeleted());
    }

    /**
     * Convert SubscriptionPlan entity to SubscriptionPlanResponse DTO
     * 
     * @param plan the subscription plan entity
     * @return the subscription plan response DTO
     */
    private SubscriptionPlanResponse mapToResponse(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .monthlyPrice(plan.getMonthlyPrice())
                .yearlyPrice(plan.getYearlyPrice())
                .userLimit(plan.getUserLimit())
                .childLimit(plan.getChildLimit())
                .coursePackageId(plan.getCoursePackage() != null ? plan.getCoursePackage().getId() : null)
                .coursePackageName(plan.getCoursePackage() != null ? plan.getCoursePackage().getPackageName() : null)
                .createdAt(plan.getCreatedAt())
                .createdBy(plan.getCreatedBy())
                .updatedAt(plan.getUpdatedAt())
                .updatedBy(plan.getUpdatedBy())
                .deletedAt(plan.getDeletedAt())
                .deletedBy(plan.getDeletedBy())
                .isDeleted(plan.getIsDeleted())
                .build();
    }
}

