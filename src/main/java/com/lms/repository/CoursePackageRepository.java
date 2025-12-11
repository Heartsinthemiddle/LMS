package com.lms.repository;

import com.lms.entity.CoursePackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoursePackageRepository extends JpaRepository<CoursePackage,Long> {

    List<CoursePackage> findByIsDeletedFalse();
}
