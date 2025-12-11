package com.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for SubscriptionPlan response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlanResponse {
    
    private Long id;
    private String name;
    private String description;
    private Double monthlyPrice;
    private Double yearlyPrice;
    private Integer userLimit;
    private Integer childLimit;
    private Long coursePackageId;
    private String coursePackageName;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
    private Boolean isDeleted;
}


