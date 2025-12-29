package com.lms.repository;

import com.lms.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Course entity operations.
 * Provides CRUD operations and custom queries for Course management.
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * Find a course by title
     * 
     * @param title the course title
     * @return Optional containing the course if found
     */
    Optional<Course> findByTitle(String title);

    /**
     * Find all active courses
     * 
     * @return list of active courses
     */
    List<Course> findByIsActiveTrue();

    /**
     * Find all courses in a specific category
     * 
     * @param category the course category
     * @return list of courses in the category
     */
    List<Course> findByCategory(String category);

    /**
     * Find all active courses in a specific category
     * 
     * @param category the course category
     * @return list of active courses in the category
     */
    List<Course> findByCategoryAndIsActiveTrue(String category);

    /**
     * Search courses by title or description (case-insensitive)
     * 
     * @param keyword the search keyword
     * @return list of matching courses
     */
    @Query("SELECT c FROM Course c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND c.isDeleted = false")
    List<Course> searchCourses(@Param("keyword") String keyword);

    /**
     * Find all non-deleted courses
     * 
     * @return list of non-deleted courses
     */
    @Query("SELECT c FROM Course c WHERE c.isDeleted = false")
    List<Course> findAllActive();

    /**
     * Check if a course exists by title
     * 
     * @param title the course title
     * @return true if course exists
     */
    boolean existsByTitle(String title);

    /**
     * Count active courses in a category
     * 
     * @param category the course category
     * @return count of active courses
     */
    long countByCategoryAndIsActiveTrue(String category);

    List<Course> findByCoursePackageIdAndIsDeletedFalse(Long id);
}

