package com.lms.controller;

import com.lms.dto.request.CreateUserSubscriptionRequest;
import com.lms.dto.request.UpdateUserSubscriptionRequest;
import com.lms.dto.response.ApiResponse;
import com.lms.dto.response.UserSubscriptionResponse;
import com.lms.service.UserSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for user subscription management endpoints.
 * Handles CRUD operations for user subscriptions with role-based access control.
 * All endpoints follow /api/v1/ standard.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/user-subscriptions")
@RequiredArgsConstructor
@Tag(name = "User Subscription Management", description = "User subscription management APIs")
@SecurityRequirement(name = "bearerAuth")
public class UserSubscriptionController {

    private final UserSubscriptionService userSubscriptionService;

    /**
     * Create a new user subscription
     * 
     * @param request the create user subscription request
     * @return ResponseEntity with the created user subscription
     */
    @Operation(
            summary = "Create a new user subscription",
            description = "Create a new subscription for a user with a specific subscription plan"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User subscription created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or user already has active subscription",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User or subscription plan not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> createUserSubscription(
            @Valid @RequestBody CreateUserSubscriptionRequest request) {
        log.info("Creating new user subscription for user ID: {}", request.getUserId());
        UserSubscriptionResponse subscription = userSubscriptionService.createUserSubscription(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User subscription created successfully", subscription));
    }

    /**
     * Get all user subscriptions
     * 
     * @return ResponseEntity with list of all user subscriptions
     */
    @Operation(
            summary = "Get all user subscriptions",
            description = "Retrieve all non-deleted user subscriptions from the system"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User subscriptions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserSubscriptionResponse>>> getAllUserSubscriptions() {
        log.info("Fetching all user subscriptions");
        List<UserSubscriptionResponse> subscriptions = userSubscriptionService.getAllUserSubscriptions();
        return ResponseEntity.ok(ApiResponse.success("User subscriptions retrieved successfully", subscriptions));
    }

    /**
     * Get a user subscription by ID
     * 
     * @param id the user subscription ID
     * @return ResponseEntity with the user subscription details
     */
    @Operation(
            summary = "Get user subscription by ID",
            description = "Retrieve a specific user subscription by its ID"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User subscription retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User subscription not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> getUserSubscriptionById(@PathVariable Long id) {
        log.info("Fetching user subscription with ID: {}", id);
        UserSubscriptionResponse subscription = userSubscriptionService.getUserSubscriptionById(id);
        return ResponseEntity.ok(ApiResponse.success("User subscription retrieved successfully", subscription));
    }

    /**
     * Get subscription by user ID
     * 
     * @param userId the user ID
     * @return ResponseEntity with the user subscription
     */
    @Operation(
            summary = "Get subscription by user ID",
            description = "Retrieve subscription for a specific user"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User subscription retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User subscription not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> getUserSubscriptionByUserId(@PathVariable Long userId) {
        log.info("Fetching user subscription for user ID: {}", userId);
        UserSubscriptionResponse subscription = userSubscriptionService.getUserSubscriptionByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("User subscription retrieved successfully", subscription));
    }

    /**
     * Get subscriptions by status
     * 
     * @param status the subscription status
     * @return ResponseEntity with list of subscriptions with the given status
     */
    @Operation(
            summary = "Get subscriptions by status",
            description = "Retrieve all subscriptions with a specific status (ACTIVE, EXPIRED, CANCELLED)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Subscriptions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<UserSubscriptionResponse>>> getSubscriptionsByStatus(
            @PathVariable String status) {
        log.info("Fetching subscriptions by status: {}", status);
        List<UserSubscriptionResponse> subscriptions = userSubscriptionService.getSubscriptionsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Subscriptions retrieved successfully", subscriptions));
    }

    /**
     * Get expiring subscriptions
     * 
     * @param endDate the end date to check (format: YYYY-MM-DD)
     * @return ResponseEntity with list of expiring subscriptions
     */
    @Operation(
            summary = "Get expiring subscriptions",
            description = "Retrieve all active subscriptions expiring before a specific date"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Expiring subscriptions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/expiring")
    public ResponseEntity<ApiResponse<List<UserSubscriptionResponse>>> getExpiringSubscriptions(
            @RequestParam(value = "endDate", required = false) LocalDate endDate) {
        LocalDate checkDate = endDate != null ? endDate : LocalDate.now().plusDays(30);
        log.info("Fetching subscriptions expiring before: {}", checkDate);
        List<UserSubscriptionResponse> subscriptions = userSubscriptionService.getExpiringSubscriptions(checkDate);
        return ResponseEntity.ok(ApiResponse.success("Expiring subscriptions retrieved successfully", subscriptions));
    }

    /**
     * Get subscriptions by plan
     * 
     * @param planId the plan ID
     * @return ResponseEntity with list of subscriptions for the plan
     */
    @Operation(
            summary = "Get subscriptions by plan",
            description = "Retrieve all subscriptions for a specific subscription plan"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Subscriptions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/plan/{planId}")
    public ResponseEntity<ApiResponse<List<UserSubscriptionResponse>>> getSubscriptionsByPlan(@PathVariable Long planId) {
        log.info("Fetching subscriptions for plan ID: {}", planId);
        List<UserSubscriptionResponse> subscriptions = userSubscriptionService.getSubscriptionsByPlan(planId);
        return ResponseEntity.ok(ApiResponse.success("Subscriptions retrieved successfully", subscriptions));
    }

    /**
     * Check if user has active subscription
     * 
     * @param userId the user ID
     * @return ResponseEntity with boolean indicating if user has active subscription
     */
    @Operation(
            summary = "Check user active subscription",
            description = "Check if a user has an active subscription"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Active subscription status retrieved",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<ApiResponse<Boolean>> hasActiveSubscription(@PathVariable Long userId) {
        log.info("Checking if user has active subscription: {}", userId);
        boolean hasActive = userSubscriptionService.hasActiveSubscription(userId);
        return ResponseEntity.ok(ApiResponse.success("Active subscription status retrieved", hasActive));
    }

    /**
     * Update a user subscription
     * 
     * @param id the user subscription ID to update
     * @param request the update user subscription request
     * @return ResponseEntity with the updated user subscription
     */
    @Operation(
            summary = "Update a user subscription",
            description = "Update an existing user subscription with the provided details"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User subscription updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User subscription not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> updateUserSubscription(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserSubscriptionRequest request) {
        log.info("Updating user subscription with ID: {}", id);
        UserSubscriptionResponse subscription = userSubscriptionService.updateUserSubscription(id, request);
        return ResponseEntity.ok(ApiResponse.success("User subscription updated successfully", subscription));
    }

    /**
     * Delete a user subscription (soft delete)
     * Marks the subscription as deleted without removing it from the database
     * 
     * @param id the user subscription ID to delete
     * @return ResponseEntity with success message
     */
    @Operation(
            summary = "Delete a user subscription",
            description = "Soft delete a user subscription (marks as deleted and status as CANCELLED)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "User subscription deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User subscription not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUserSubscription(@PathVariable Long id) {
        log.info("Deleting user subscription with ID: {}", id);
        userSubscriptionService.deleteUserSubscription(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Restore a deleted user subscription
     * 
     * @param id the user subscription ID to restore
     * @return ResponseEntity with the restored user subscription
     */
    @Operation(
            summary = "Restore a deleted user subscription",
            description = "Restore a soft-deleted user subscription back to active status"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User subscription restored successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User subscription not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "User subscription is not deleted",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> restoreUserSubscription(@PathVariable Long id) {
        log.info("Restoring user subscription with ID: {}", id);
        UserSubscriptionResponse subscription = userSubscriptionService.restoreUserSubscription(id);
        return ResponseEntity.ok(ApiResponse.success("User subscription restored successfully", subscription));
    }
}

