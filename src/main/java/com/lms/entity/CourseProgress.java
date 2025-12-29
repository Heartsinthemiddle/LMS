package com.lms.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseProgress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    private String registrationId;     // Rustici registration ID
    private String status;             // completed, incomplete, passed, failed
    private Double score;              // Overall score
    @Lob
    private String suspendData;        // SCORM suspend data
    @Lob
    private String cmiData;            // Full CMI JSON

    private Integer attemptNumber;     // Attempt number

}

