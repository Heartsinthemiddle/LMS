package com.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing course.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCourseRequest {
    
    private String title;
    
    private String description;
    
    private String category;

    private String videoUrl;
    private Integer durationSeconds;
    
    private Boolean isActive;

    private Long coursePackageId;
}


