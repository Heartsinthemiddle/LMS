package com.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Course response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {
    
    private Long id;
    private String title;
    private String description;
    private String category;
    private String videoUrl;
    private Integer durationSeconds;
    private Boolean isActive;
    private Long coursePackageId;
    private String coursePackageName;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
    private boolean isDeleted;
}

