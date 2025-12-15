package com.lms.service;

import com.lms.dto.request.CreateCoursePackageRequest;
import com.lms.dto.request.CreateCoursePackageWithCoursesRequest;
import com.lms.dto.request.UpdateCoursePackageRequest;
import com.lms.dto.response.CoursePackageResponse;
import com.lms.entity.Course;
import com.lms.entity.CoursePackage;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.CoursePackageRepository;
import com.lms.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoursePackageService {

    private final CoursePackageRepository coursePackageRepository;
    private final CourseRepository courseRepository;

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }

    @Transactional
    public CoursePackageResponse createCoursePackage(CreateCoursePackageRequest req) {
        log.info("Creating course package: {}", req.getPackageName());

        CoursePackage coursePackage = new CoursePackage();
        coursePackage.setPackageName(req.getPackageName());
        coursePackage.setDescription(req.getDescription());
        coursePackage.setMonthlyPrice(req.getMonthlyPrice());
        coursePackage.setYearlyPrice(req.getYearlyPrice());
        coursePackage.setStripeMonthlyPriceId(req.getStripeMonthlyPriceId());
        coursePackage.setStripeYearlyPriceId(req.getStripeYearlyPriceId());
        coursePackage.setChildLimit(req.getChildLimit());
        coursePackage.setCourseLimit(req.getCourseLimit());
        coursePackage.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);
        coursePackage.setIsDeleted(false);
        coursePackage.setCreatedBy(getCurrentUser());
        coursePackageRepository.save(coursePackage);

        return map(coursePackage);
    }

    /**
     * Create a course package with assigned courses
     *
     * @param req the request containing package details and course IDs
     * @return the created course package response
     */
    @Transactional
    public CoursePackageResponse createCoursePackageWithCourses(CreateCoursePackageWithCoursesRequest req) {
        log.info("Creating course package with courses: {}", req.getPackageName());

        // Validate that courseIds list is not empty
        if (req.getCourseIds() == null || req.getCourseIds().isEmpty()) {
            throw new IllegalArgumentException("At least one course must be assigned to the package");
        }

        // Validate that course limit is sufficient for the number of courses
        if (req.getCourseIds().size() > req.getCourseLimit()) {
            throw new IllegalArgumentException("Number of courses (" + req.getCourseIds().size() +
                    ") cannot exceed course limit (" + req.getCourseLimit() + ")");
        }

        // Verify all courses exist and are not deleted
        List<Course> courses = courseRepository.findAllById(req.getCourseIds());

        if (courses.size() != req.getCourseIds().size()) {
            throw new ResourceNotFoundException("One or more courses not found");
        }

        // Check if any course is deleted
        boolean anyDeleted = courses.stream().anyMatch(c -> Boolean.TRUE.equals(c.getIsDeleted()));
        if (anyDeleted) {
            throw new IllegalArgumentException("One or more selected courses are deleted");
        }

        // Create the course package
        CoursePackage coursePackage = new CoursePackage();
        coursePackage.setPackageName(req.getPackageName());
        coursePackage.setDescription(req.getDescription());
        coursePackage.setMonthlyPrice(req.getMonthlyPrice());
        coursePackage.setYearlyPrice(req.getYearlyPrice());
        coursePackage.setStripeMonthlyPriceId(req.getStripeMonthlyPriceId());
        coursePackage.setStripeYearlyPriceId(req.getStripeYearlyPriceId());
        coursePackage.setChildLimit(req.getChildLimit());
        coursePackage.setCourseLimit(req.getCourseLimit());
        coursePackage.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);
        coursePackage.setIsDeleted(false);
        coursePackage.setCreatedBy(getCurrentUser());

        CoursePackage savedPackage = coursePackageRepository.save(coursePackage);
        log.info("Course package created with ID: {} and {} courses assigned", savedPackage.getId(), courses.size());

        // Assign courses to the package
        for (Course course : courses) {
            course.setCoursePackage(savedPackage);
            courseRepository.save(course);
            log.debug("Assigned course {} to package {}", course.getId(), savedPackage.getId());
        }

        return map(savedPackage);
    }

    @Transactional(readOnly = true)
    public List<CoursePackageResponse> getAllCoursePackages() {
        return coursePackageRepository.findByIsDeletedFalse()
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public CoursePackageResponse getCoursePackageById(Long id) {
        CoursePackage coursePackage = coursePackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course package not found"));

        if (Boolean.TRUE.equals(coursePackage.getIsDeleted())) {
            throw new ResourceNotFoundException("Course package not found");
        }

        return map(coursePackage);
    }

    @Transactional
    public CoursePackageResponse updateCoursePackage(Long id, UpdateCoursePackageRequest req) {
        CoursePackage coursePackage = coursePackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course package not found"));

        if (Boolean.TRUE.equals(coursePackage.getIsDeleted())) {
            throw new ResourceNotFoundException("Course package not found");
        }

        if (req.getPackageName() != null) {
            coursePackage.setPackageName(req.getPackageName());
        }
        if (req.getDescription() != null) {
            coursePackage.setDescription(req.getDescription());
        }
        if (req.getMonthlyPrice() != null) {
            coursePackage.setMonthlyPrice(req.getMonthlyPrice());
        }
        if (req.getYearlyPrice() != null) {
            coursePackage.setYearlyPrice(req.getYearlyPrice());
        }
        if (req.getStripeMonthlyPriceId() != null) {
            coursePackage.setStripeMonthlyPriceId(req.getStripeMonthlyPriceId());
        }
        if (req.getStripeYearlyPriceId() != null) {
            coursePackage.setStripeYearlyPriceId(req.getStripeYearlyPriceId());
        }
        if (req.getChildLimit() != null) {
            coursePackage.setChildLimit(req.getChildLimit());
        }
        if (req.getCourseLimit() != null) {
            coursePackage.setCourseLimit(req.getCourseLimit());
        }
        if (req.getIsActive() != null) {
            coursePackage.setIsActive(req.getIsActive());
        }

        coursePackage.setUpdatedBy(getCurrentUser());

        return map(coursePackageRepository.save(coursePackage));
    }

    @Transactional
    public void deleteCoursePackage(Long id) {
        CoursePackage coursePackage = coursePackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course package not found"));

        if (Boolean.TRUE.equals(coursePackage.getIsDeleted())) {
            throw new ResourceNotFoundException("Course package not found");
        }

        coursePackage.setIsDeleted(true);
        coursePackage.setDeletedAt(LocalDateTime.now());
        coursePackage.setDeletedBy(getCurrentUser());

        coursePackageRepository.save(coursePackage);
    }

    @Transactional
    public CoursePackageResponse restoreCoursePackage(Long id) {
        CoursePackage coursePackage = coursePackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course package not found"));

        if (!Boolean.TRUE.equals(coursePackage.getIsDeleted())) {
            throw new IllegalArgumentException("Course package is not deleted");
        }

        coursePackage.setIsDeleted(false);
        coursePackage.setDeletedAt(null);
        coursePackage.setDeletedBy(null);
        coursePackage.setUpdatedBy(getCurrentUser());

        return map(coursePackageRepository.save(coursePackage));
    }

    private CoursePackageResponse map(CoursePackage coursePackage) {
        return CoursePackageResponse.builder()
                .id(coursePackage.getId())
                .packageName(coursePackage.getPackageName())
                .description(coursePackage.getDescription())
                .monthlyPrice(coursePackage.getMonthlyPrice())
                .yearlyPrice(coursePackage.getYearlyPrice())
                .stripeMonthlyPriceId(coursePackage.getStripeMonthlyPriceId())
                .stripeYearlyPriceId(coursePackage.getStripeYearlyPriceId())
                .childLimit(coursePackage.getChildLimit())
                .courseLimit(coursePackage.getCourseLimit())
                .isActive(coursePackage.getIsActive())
                .createdAt(coursePackage.getCreatedAt())
                .createdBy(coursePackage.getCreatedBy())
                .updatedAt(coursePackage.getUpdatedAt())
                .updatedBy(coursePackage.getUpdatedBy())
                .deletedAt(coursePackage.getDeletedAt())
                .deletedBy(coursePackage.getDeletedBy())
                .isDeleted(coursePackage.getIsDeleted())
                .build();
    }
}
