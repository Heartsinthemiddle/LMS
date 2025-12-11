package com.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoursePackageResponse {

    private Long id;
    private String packageName;
    private String description;
    private Double monthlyPrice;
    private Double yearlyPrice;
    private String stripeMonthlyPriceId;
    private String stripeYearlyPriceId;
    private Integer childLimit;
    private Integer courseLimit;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
    private Boolean isDeleted;
}

