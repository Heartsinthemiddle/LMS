package com.lms.service;

import com.lms.dto.request.CreateCourseRequest;
import com.lms.dto.request.UpdateCourseRequest;
import com.lms.dto.response.CourseResponse;
import com.lms.entity.Course;
import com.lms.entity.CoursePackage;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.CourseRepository;
import com.lms.repository.CoursePackageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CoursePackageRepository coursePackageRepository;


    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }

    @Transactional
    public CourseResponse createCourse(CreateCourseRequest req) {
        log.info("Creating course: {}", req.getTitle());

        if (courseRepository.findByTitle(req.getTitle()).isPresent()) {
            throw new IllegalArgumentException("Course with title already exists");
        }

        CoursePackage coursePackage = coursePackageRepository.findById(req.getCoursePackageId())
                .orElseThrow(() -> new ResourceNotFoundException("Course package not found"));

        Course course = new Course();
        course.setTitle(req.getTitle());
        course.setDescription(req.getDescription());
        course.setCategory(req.getCategory());
        course.setVideoUrl(req.getVideoUrl());
        course.setDurationSeconds(req.getDurationSeconds());
        course.setCoursePackage(coursePackage);
        course.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);

        course.setCreatedBy(getCurrentUser());
        course.setIsDeleted(false);

        Course saved = courseRepository.save(course);
        return map(saved);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        List<Course> courses = courseRepository.findAllActive();
        return courses.stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (course.getIsDeleted())
            throw new ResourceNotFoundException("Course not found");

        return map(course);
    }

    @Transactional
    public CourseResponse updateCourse(Long id, UpdateCourseRequest req) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (course.getIsDeleted())
            throw new ResourceNotFoundException("Course not found");

        if (req.getTitle() != null)
            course.setTitle(req.getTitle());

        if (req.getDescription() != null)
            course.setDescription(req.getDescription());

        if (req.getCategory() != null)
            course.setCategory(req.getCategory());

        if (req.getVideoUrl() != null)
            course.setVideoUrl(req.getVideoUrl());

        if (req.getDurationSeconds() != null)
            course.setDurationSeconds(req.getDurationSeconds());

        if (req.getCoursePackageId() != null) {
            CoursePackage pkg = coursePackageRepository.findById(req.getCoursePackageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course package not found"));
            course.setCoursePackage(pkg);
        }

        if (req.getIsActive() != null)
            course.setIsActive(req.getIsActive());

        course.setUpdatedBy(getCurrentUser());

        return map(courseRepository.save(course));
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course c = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (c.getIsDeleted())
            throw new ResourceNotFoundException("Course not found");

        c.setIsDeleted(true);
        c.setDeletedAt(LocalDateTime.now());
        c.setDeletedBy(getCurrentUser());

        courseRepository.save(c);
    }

    @Transactional
    public CourseResponse restoreCourse(Long id) {
        Course c = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!c.getIsDeleted())
            throw new IllegalArgumentException("Course is not deleted");

        c.setIsDeleted(false);
        c.setDeletedAt(null);
        c.setDeletedBy(null);
        c.setUpdatedBy(getCurrentUser());

        return map(courseRepository.save(c));
    }

    private CourseResponse map(Course c) {
        return CourseResponse.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .category(c.getCategory())
                .videoUrl(c.getVideoUrl())
                .durationSeconds(c.getDurationSeconds())
                .coursePackageId(c.getCoursePackage().getId())
                .coursePackageName(c.getCoursePackage().getPackageName())
                .isActive(c.getIsActive())
                .createdAt(c.getCreatedAt())
                .createdBy(c.getCreatedBy())
                .updatedAt(c.getUpdatedAt())
                .updatedBy(c.getUpdatedBy())
                .deletedAt(c.getDeletedAt())
                .deletedBy(c.getDeletedBy())
                .isDeleted(c.getIsDeleted())
                .build();
    }
}
