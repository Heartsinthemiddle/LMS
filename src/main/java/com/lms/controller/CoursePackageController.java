package com.lms.controller;

import com.lms.dto.request.CreateCoursePackageRequest;
import com.lms.dto.request.CreateCoursePackageWithCoursesRequest;
import com.lms.dto.request.UpdateCoursePackageRequest;
import com.lms.dto.response.ApiResponse;
import com.lms.dto.response.CoursePackageResponse;
import com.lms.service.CoursePackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/course-packages")
@RequiredArgsConstructor
@Tag(name = "Course Package Management", description = "Course package CRUD APIs")
@SecurityRequirement(name = "bearerAuth")
public class CoursePackageController {

    private final CoursePackageService coursePackageService;

    @Operation(summary = "Create course package")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Course package created",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CoursePackageResponse>> createCoursePackage(
            @Valid @RequestBody CreateCoursePackageRequest request) {
        CoursePackageResponse response = coursePackageService.createCoursePackage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course package created", response));
    }

    @Operation(
            summary = "Create course package with assigned courses",
            description = "Create a new course package and assign multiple courses to it in a single transaction"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Course package created with courses assigned",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - missing courses or course limit exceeded",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "One or more courses not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping("/with-courses")
    public ResponseEntity<ApiResponse<CoursePackageResponse>> createCoursePackageWithCourses(
            @Valid @RequestBody CreateCoursePackageWithCoursesRequest request) {
        log.info("Creating course package with {} courses", request.getCourseIds().size());
        CoursePackageResponse response = coursePackageService.createCoursePackageWithCourses(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course package created with courses assigned successfully", response));
    }

    @Operation(summary = "Get all course packages")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CoursePackageResponse>>> getAllCoursePackages() {
        List<CoursePackageResponse> packages = coursePackageService.getAllCoursePackages();
        return ResponseEntity.ok(ApiResponse.success("Course packages retrieved", packages));
    }

    @Operation(summary = "Get course package by id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CoursePackageResponse>> getCoursePackageById(@PathVariable Long id) {
        CoursePackageResponse response = coursePackageService.getCoursePackageById(id);
        return ResponseEntity.ok(ApiResponse.success("Course package retrieved", response));
    }

    @Operation(summary = "Update course package")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CoursePackageResponse>> updateCoursePackage(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCoursePackageRequest request) {
        CoursePackageResponse response = coursePackageService.updateCoursePackage(id, request);
        return ResponseEntity.ok(ApiResponse.success("Course package updated", response));
    }

    @Operation(summary = "Delete course package")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoursePackage(@PathVariable Long id) {
        coursePackageService.deleteCoursePackage(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Restore course package")
    @PostMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<CoursePackageResponse>> restoreCoursePackage(@PathVariable Long id) {
        CoursePackageResponse response = coursePackageService.restoreCoursePackage(id);
        return ResponseEntity.ok(ApiResponse.success("Course package restored", response));
    }
}
