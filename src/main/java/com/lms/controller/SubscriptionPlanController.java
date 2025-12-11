package com.lms.controller;

import com.lms.dto.request.CreateSubscriptionPlanRequest;
import com.lms.dto.request.UpdateSubscriptionPlanRequest;
import com.lms.dto.response.ApiResponse;
import com.lms.dto.response.SubscriptionPlanResponse;
import com.lms.service.SubscriptionPlanService;
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
import java.util.List;

/**
 * REST controller for subscription plan management endpoints.
 * Handles CRUD operations for subscription plans with role-based access control.
 * All endpoints follow /api/v1/ standard.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/subscription-plans")
@RequiredArgsConstructor
@Tag(name = "Subscription Plan Management", description = "Subscription plan management APIs")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    /**
     * Create a new subscription plan
     * 
     * @param request the create subscription plan request
     * @return ResponseEntity with the created subscription plan
     */
    @Operation(
            summary = "Create a new subscription plan",
            description = "Create a new subscription plan with the provided details and associate it with a course package"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Subscription plan created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or plan already exists",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Course package not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> createSubscriptionPlan(
            @Valid @RequestBody CreateSubscriptionPlanRequest request) {
        log.info("Creating new subscription plan with name: {}", request.getName());
        SubscriptionPlanResponse plan = subscriptionPlanService.createSubscriptionPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscription plan created successfully", plan));
    }

    /**
     * Get all subscription plans
     * 
     * @return ResponseEntity with list of all subscription plans
     */
    @Operation(
            summary = "Get all subscription plans",
            description = "Retrieve all non-deleted subscription plans from the system"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Subscription plans retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getAllSubscriptionPlans() {
        log.info("Fetching all subscription plans");
        List<SubscriptionPlanResponse> plans = subscriptionPlanService.getAllSubscriptionPlans();
        return ResponseEntity.ok(ApiResponse.success("Subscription plans retrieved successfully", plans));
    }

    /**
     * Get a subscription plan by ID
     * 
     * @param id the subscription plan ID
     * @return ResponseEntity with the subscription plan details
     */
    @Operation(
            summary = "Get subscription plan by ID",
            description = "Retrieve a specific subscription plan by its ID"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Subscription plan retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Subscription plan not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> getSubscriptionPlanById(@PathVariable Long id) {
        log.info("Fetching subscription plan with ID: {}", id);
        SubscriptionPlanResponse plan = subscriptionPlanService.getSubscriptionPlanById(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan retrieved successfully", plan));
    }

    /**
     * Search subscription plans by keyword
     * 
     * @param keyword the search keyword
     * @return ResponseEntity with list of matching subscription plans
     */
    @Operation(
            summary = "Search subscription plans",
            description = "Search subscription plans by name or description using a keyword"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Subscription plans retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> searchSubscriptionPlans(
            @RequestParam String keyword) {
        log.info("Searching subscription plans with keyword: {}", keyword);
        List<SubscriptionPlanResponse> plans = subscriptionPlanService.searchSubscriptionPlans(keyword);
        return ResponseEntity.ok(ApiResponse.success("Subscription plans retrieved successfully", plans));
    }

    /**
     * Update a subscription plan
     * 
     * @param id the subscription plan ID to update
     * @param request the update subscription plan request
     * @return ResponseEntity with the updated subscription plan
     */
    @Operation(
            summary = "Update a subscription plan",
            description = "Update an existing subscription plan with the provided details"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Subscription plan updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Subscription plan not found",
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
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> updateSubscriptionPlan(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubscriptionPlanRequest request) {
        log.info("Updating subscription plan with ID: {}", id);
        SubscriptionPlanResponse plan = subscriptionPlanService.updateSubscriptionPlan(id, request);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan updated successfully", plan));
    }

    /**
     * Delete a subscription plan (soft delete)
     * Marks the plan as deleted without removing it from the database
     * 
     * @param id the subscription plan ID to delete
     * @return ResponseEntity with success message
     */
    @Operation(
            summary = "Delete a subscription plan",
            description = "Soft delete a subscription plan (marks as deleted without removing from database)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Subscription plan deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Subscription plan not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubscriptionPlan(@PathVariable Long id) {
        log.info("Deleting subscription plan with ID: {}", id);
        subscriptionPlanService.deleteSubscriptionPlan(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Restore a deleted subscription plan
     * 
     * @param id the subscription plan ID to restore
     * @return ResponseEntity with the restored subscription plan
     */
    @Operation(
            summary = "Restore a deleted subscription plan",
            description = "Restore a soft-deleted subscription plan back to active status"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Subscription plan restored successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Subscription plan not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Subscription plan is not deleted",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> restoreSubscriptionPlan(@PathVariable Long id) {
        log.info("Restoring subscription plan with ID: {}", id);
        SubscriptionPlanResponse plan = subscriptionPlanService.restoreSubscriptionPlan(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan restored successfully", plan));
    }
}

