package com.lms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCoursePackageRequest {

    private String packageName;
    private String description;
    private Double monthlyPrice;
    private Double yearlyPrice;
    private String stripeMonthlyPriceId;
    private String stripeYearlyPriceId;
    private Integer childLimit;
    private Integer courseLimit;
    private Boolean isActive;
}

