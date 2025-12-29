package com.lms.repository;

import com.lms.entity.Course;
import com.lms.entity.Enrollment;
import com.lms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Enrollment entity operations.
 * Provides CRUD operations and custom queries for Enrollment management.
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /**
     * Find all enrollments for a specific child/user
     * 
     * @param child the user/child
     * @return list of enrollments for the child
     */
    List<Enrollment> findByChild(User child);

    /**
     * Find all enrollments for a specific course
     * 
     * @param course the course
     * @return list of enrollments for the course
     */
    List<Enrollment> findByCourse(Course course);

    /**
     * Find enrollment by child and course
     * 
     * @param child the user/child
     * @param course the course
     * @return Optional containing the enrollment if found
     */
    Optional<Enrollment> findByChildAndCourse(User child, Course course);

    /**
     * Find all completed enrollments for a child
     * 
     * @param child the user/child
     * @return list of completed enrollments
     */
    List<Enrollment> findByChildAndIsCompletedTrue(User child);

    /**
     * Find all in-progress enrollments for a child
     * 
     * @param child the user/child
     * @return list of in-progress enrollments
     */
    List<Enrollment> findByChildAndIsCompletedFalse(User child);

    /**
     * Find all enrollments for a course that are completed
     * 
     * @param course the course
     * @return list of completed enrollments
     */
    List<Enrollment> findByCourseAndIsCompletedTrue(Course course);

    /**
     * Find enrollments by progress percentage range
     * 
     * @param minProgress minimum progress percentage
     * @param maxProgress maximum progress percentage
     * @return list of enrollments in the progress range
     */
    @Query("SELECT e FROM Enrollment e WHERE e.progressPercentage >= :minProgress AND e.progressPercentage <= :maxProgress")
    List<Enrollment> findByProgressRange(@Param("minProgress") int minProgress, @Param("maxProgress") int maxProgress);

    /**
     * Count completed enrollments for a course
     * 
     * @param course the course
     * @return count of completed enrollments
     */
    long countByCourseAndIsCompletedTrue(Course course);

    /**
     * Count total enrollments for a course
     * 
     * @param course the course
     * @return count of enrollments
     */
    long countByCourse(Course course);

    /**
     * Count completed enrollments for a child
     * 
     * @param child the user/child
     * @return count of completed enrollments
     */
    long countByChildAndIsCompletedTrue(User child);
    @Query(value = "select * from enrollments where id =8",nativeQuery = true)
    Enrollment findBYExactId(Long id);

    /**
     * Check if a child is already enrolled in a course
     * 
     * @param child the user/child
     * @param course the course
     * @return true if child is enrolled
     */
    boolean existsByChildAndCourse(User child, Course course);

    Optional<Enrollment> findByIdAndCourseIdAndIsDeletedFalse(Long enrollmentId, Long courseId);

    /**
     * Find all non-deleted enrollments for a child
     * 
     * @param child the user/child
     * @return list of non-deleted enrollments
     */
//    @Query("SELECT e FROM Enrollment e WHERE e.child = :child AND e.isDeleted = false")
//    List<Enrollment> findActiveEnrollmentsByChild(@Param("child") User child);
//
//    /**
//     * Find all non-deleted enrollments for a course
//     *
//     * @param course the course
//     * @return list of non-deleted enrollments
//     */
//    @Query("SELECT e FROM Enrollment e WHERE e.course = :course AND e.isDeleted = false")
//    List<Enrollment> findActiveEnrollmentsByCourse(@Param("course") Course course);
}

