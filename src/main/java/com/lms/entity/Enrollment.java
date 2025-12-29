package com.lms.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "child_id", nullable = true)
    private Child child;

    @ManyToOne(optional = false)
    @JoinColumn(name = "parent_id", nullable = true)
    private Parent parent;


    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "progress_percentage")
    private int progressPercentage;

    @Column(name = "is_completed")
    private Boolean isCompleted;

    @Column(name = "scorm_registration_id", unique = true,nullable = true)
    private String scormRegistrationId;

    @Column(name = "scorm_course_id")
    private String scormCourseId;
    private String learnerId;
    @Enumerated(EnumType.STRING)
    private LearnerType learnerType; // ENROLLED, IN_PROGRESS, COMPLETED, DROPP

}


