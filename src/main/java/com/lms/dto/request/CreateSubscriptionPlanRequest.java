package com.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new subscription plan.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSubscriptionPlanRequest {

    @NotBlank(message = "Plan name is required")
    private String name;

    @NotBlank(message = "Plan description is required")
    private String description;

    @NotNull(message = "Monthly price is required")
    private Double monthlyPrice;

    @NotNull(message = "Yearly price is required")
    private Double yearlyPrice;

    @NotNull(message = "User limit is required")
    private Integer userLimit;

    @NotNull(message = "Child limit is required")
    private Integer childLimit;

    @NotNull(message = "Course package ID is required")
    private Long coursePackageId;
}