package com.lms.repository;

import com.lms.entity.CourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseProgressRepository extends JpaRepository<CourseProgress,Long> {
    List<CourseProgress> findByEnrollmentIdOrderByAttemptNumberDesc(Long enrollmentId);
}
