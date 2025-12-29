package com.lms.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Course extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String category;

    private String videoUrl;     // The actual video file (S3 / Cloudflare / Video CDN)

    @Column(name = "duration_seconds")
    private Integer durationSeconds;


    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne(optional = true)
    @JoinColumn(name = "course_package_id", nullable = true)
    private CoursePackage coursePackage;

    @Enumerated(EnumType.STRING)
    private CourseType courseType;  // VIDEO, SCORM, etc.

    private String scormCourseId;     // Rustici SCORM Cloud Course ID
    private String scormLaunchUrl;    // Rustici launch link returned by API
    private Boolean isScorm;
}
