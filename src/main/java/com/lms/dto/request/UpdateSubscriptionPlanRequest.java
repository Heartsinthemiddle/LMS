package com.lms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing subscription plan.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSubscriptionPlanRequest {
    
    private String name;
    
    private String description;
    
    private Double monthlyPrice;
    
    private Double yearlyPrice;
    
    private Integer userLimit;
    
    private Integer childLimit;
    
    private Long coursePackageId;
}

