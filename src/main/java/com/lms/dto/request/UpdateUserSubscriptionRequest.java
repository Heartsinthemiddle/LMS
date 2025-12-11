package com.lms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for updating an existing user subscription.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserSubscriptionRequest {
    
    private Long planId;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private String stripeCustomerId;
    
    private String stripeSubscriptionId;
    
    private String status;
}

