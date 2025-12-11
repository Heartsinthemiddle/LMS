package com.lms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for creating a new user subscription.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserSubscriptionRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Subscription plan ID is required")
    private Long planId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private String stripeCustomerId;

    private String stripeSubscriptionId;

    private String status = "ACTIVE";
}