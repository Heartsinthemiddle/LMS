package com.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for creating a course package with assigned courses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCoursePackageWithCoursesRequest {

    @NotBlank(message = "Package name is required")
    private String packageName;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Monthly price is required")
    private Double monthlyPrice;

    @NotNull(message = "Yearly price is required")
    private Double yearlyPrice;

    private String stripeMonthlyPriceId;
    private String stripeYearlyPriceId;

    @NotNull(message = "Child limit is required")
    private Integer childLimit;

    @NotNull(message = "Course limit is required")
    private Integer courseLimit;

    private Boolean isActive = true;

    @NotNull(message = "Course IDs list is required")
    private List<Long> courseIds;
}

