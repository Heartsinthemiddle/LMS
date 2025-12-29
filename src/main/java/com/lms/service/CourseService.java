package com.lms.service;

import com.lms.dto.request.CreateCourseRequest;
import com.lms.dto.request.UpdateCourseRequest;
import com.lms.dto.response.CourseResponse;
import com.lms.entity.*;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.*;

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
    private final EnrollmentRepository enrollmentRepository;
    private final RusticiClient rusticiClient;
    private final QuizIntractionRepository quizIntractionRepository;
    private final CourseProgressRepository courseProgressRepository;


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

    @Transactional
    public String startCourse(Long courseId, Long enrollmentId) {



        Enrollment enrollment = enrollmentRepository.findByIdAndCourseIdAndIsDeletedFalse(enrollmentId,courseId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        Course course = enrollment.getCourse();

        if (!Boolean.TRUE.equals(course.getIsScorm())) {
            throw new IllegalStateException("Course is not a SCORM course");
        }

        // 🔑 Determine learner
        String learnerId;
        String firstName;
        String email;

        if (enrollment.getChild() != null) {
            Child child = enrollment.getChild();
            learnerId = "child_" + child.getId();
            firstName = child.getName();
            email = child.getUserName();
        } else {
            Parent user = enrollment.getParent();
            learnerId = "parent_" + user.getId();
            firstName = user.getUserName();
            email = user.getUserEmail();
        }

        // 🔑 Registration ID
        String registrationId =
                course.getScormCourseId() + "_" + learnerId;

        // 1️⃣ Create registration (idempotent)
        rusticiClient.createRegistration(
                course.getScormCourseId(),
                registrationId,
                learnerId,
                firstName,
                "Learner",
                email
        ).block();

        // 2️⃣ Generate launch URL
        String launchUrl =
                rusticiClient.getLaunchLink(registrationId).block();

        // 3️⃣ Save registration ID
        enrollment.setScormRegistrationId(registrationId);
        enrollment.setLearnerId(learnerId);
        enrollmentRepository.save(enrollment);

        return launchUrl;
    }

    @Transactional
    public Map<String, Object> trackCourseProgress(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        String registrationId = enrollment.getScormRegistrationId();
        if (registrationId == null) {
            throw new IllegalStateException("SCORM registration not found for this enrollment");
        }


        Map<String, Object> progress = rusticiClient.getRegistrationProgress(registrationId).block();
        List<Map<String, Object>> statements = rusticiClient.getRegistrationStatements(registrationId).block();

        Map<String, Object> result = new HashMap<>();
        result.put("progress", progress);
        result.put("quizzes", statements);

        return result;
    }

    @Transactional
    public CourseProgress updateCourseProgress(Long enrollmentId) {
    Enrollment enrollment = enrollmentRepository.findBYExactId(enrollmentId);
        if (Objects.isNull(enrollment))
            throw new IllegalStateException("Enrollment not found");

        String registrationId = enrollment.getScormRegistrationId();
        if (registrationId == null)
            throw new IllegalStateException("SCORM registration not found");

        // Fetch progress & statements
        Map<String, Object> progressData = rusticiClient.getRegistrationProgress(registrationId).block();
        List<Map<String, Object>> statements = rusticiClient.getRegistrationStatements(registrationId).block();

        // Determine attempt number
        List<CourseProgress> previousAttempts = courseProgressRepository.findByEnrollmentIdOrderByAttemptNumberDesc(enrollmentId);
        int attemptNumber = previousAttempts.isEmpty() ? 1 : previousAttempts.get(0).getAttemptNumber() + 1;

        // Save CourseProgress
        CourseProgress progress = CourseProgress.builder()
                .enrollment(enrollment)
                .registrationId(registrationId)
                .status((String) progressData.getOrDefault("completionStatus", "incomplete"))
                .score(progressData.get("score") != null ? ((Number) progressData.get("score")).doubleValue() : null)
                .suspendData((String) progressData.getOrDefault("suspendData", ""))
                .cmiData(progressData.get("cmi") != null ? progressData.get("cmi").toString() : "")
                .attemptNumber(attemptNumber)
                .build();

        courseProgressRepository.save(progress);

        // Save QuizInteractions
        statements.forEach(statement -> {
            QuizInteraction quiz = QuizInteraction.builder()
                    .courseProgress(progress)
                    .interactionId((String) statement.get("id"))
                    .type((String) statement.get("type"))
                    .studentResponse((String) statement.get("studentResponse"))
                    .correctResponses(statement.get("correctResponses") != null ? statement.get("correctResponses").toString() : "")
                    .result((String) statement.get("result"))
                    .description((String) statement.get("description"))
                    .createdAt(LocalDateTime.now())
                    .build();
            quizIntractionRepository.save(quiz);
        });

        return progress;
    }


}
