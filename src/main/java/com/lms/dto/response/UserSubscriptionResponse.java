package com.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for UserSubscription response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscriptionResponse {
    
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long planId;
    private String planName;
    private Double monthlyPrice;
    private Double yearlyPrice;
    private String stripeCustomerId;
    private String stripeSubscriptionId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
    private Boolean isDeleted;
}


