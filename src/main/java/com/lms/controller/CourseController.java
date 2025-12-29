package com.lms.controller;

import com.lms.dto.request.CreateCourseRequest;
import com.lms.dto.request.ScormUploadRequest;
import com.lms.dto.request.UpdateCourseRequest;
import com.lms.dto.response.ApiResponse;
import com.lms.dto.response.CourseResponse;
import com.lms.dto.response.ImportJobStatus;
import com.lms.entity.Course;
import com.lms.entity.CourseProgress;
import com.lms.entity.CourseType;
import com.lms.repository.CourseRepository;
import com.lms.service.CourseService;
import com.lms.service.RusticiClient;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Course Management", description = "Course CRUD APIs")
@SecurityRequirement(name = "bearerAuth")
public class CourseController {

    private final CourseService courseService;
    private final CourseRepository courseRepository;
    private final RusticiClient rusticiClient;

    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @Valid @RequestBody CreateCourseRequest request) {

        log.info("Creating new course: {}", request.getTitle());

        CourseResponse course = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course created", course));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAllCourses() {
        List<CourseResponse> courses = courseService.getAllCourses();
        return ResponseEntity.ok(ApiResponse.success("Courses retrieved", courses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable Long id) {
        CourseResponse course = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success("Course retrieved", course));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequest request) {

        CourseResponse course = courseService.updateCourse(id, request);
        return ResponseEntity.ok(ApiResponse.success("Course updated", course));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<CourseResponse>> restoreCourse(@PathVariable Long id) {
        CourseResponse course = courseService.restoreCourse(id);
        return ResponseEntity.ok(ApiResponse.success("Course restored", course));
    }

    @Operation(
            summary = "Upload SCORM ZIP",
            description = "Uploads a SCORM package to Rustici Cloud and returns updated course info."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "SCORM uploaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Course not found")
    })
    @PostMapping(
            value = "/scorm",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Course> uploadScormCourse(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam CourseType courseType
            ) throws Exception {

        // 1️⃣ Unique SCORM Course ID
        String scormCourseId = "lms_scorm_" + UUID.randomUUID();

        // 2️⃣ Upload SCORM ZIP
        String importJobId = rusticiClient
                .uploadScormZip(file, scormCourseId)
                .block();

        // 3️⃣ Wait for completion
        ImportJobStatus status;
        do {
            status = rusticiClient.getImportJobStatus(importJobId).block();
            Thread.sleep(1000);
        } while (!"COMPLETE".equalsIgnoreCase(status.getStatus()));

        // 4️⃣ Launch URL (optional)


        // 5️⃣ Create Course ONLY
        Course course = new Course();
        course.setTitle(
                title != null ? title : file.getOriginalFilename()
        );
        course.setDescription(description);
        course.setCategory(category);

        course.setCourseType(courseType);
        course.setIsScorm(true);
        course.setScormCourseId(scormCourseId);
        course.setScormLaunchUrl(null);

        course.setIsActive(true);
        course.setCoursePackage(null); // explicitly optional

        courseRepository.save(course);

        return ResponseEntity.ok(course);
    }

    @PostMapping("/{courseId}/start")
    public ResponseEntity<ApiResponse<String>> startCourse(
            @PathVariable Long courseId,
            @RequestParam Long enrollmentId
    ) {

        String launchUrl = courseService.startCourse(courseId, enrollmentId);

        return ResponseEntity.ok(
                ApiResponse.success("Course launched", launchUrl)
        );
    }

    @PostMapping("/{courseId}/track")
    public ResponseEntity<ApiResponse<CourseProgress>> trackCourseProgress(
            @PathVariable Long courseId,
            @RequestParam Long enrollmentId
    ) {
        CourseProgress progress = courseService.updateCourseProgress(enrollmentId);
        return ResponseEntity.ok(ApiResponse.success("Course progress updated", progress));
    }













}
