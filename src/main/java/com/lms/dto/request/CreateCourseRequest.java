package com.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new course.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCourseRequest {

    @NotBlank(message = "Course title is required")
    private String title;

    @NotBlank(message = "Course description is required")
    private String description;

    @NotBlank(message = "Course category is required")
    private String category;

    private String videoUrl;
    private Integer durationSeconds;

    @NotNull(message = "Course package ID is required")
    private Long coursePackageId;

    private Boolean isActive = true;
}