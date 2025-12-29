package com.lms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_interaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizInteraction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_progress_id", nullable = false)
    private CourseProgress courseProgress;

    private String interactionId;      // SCORM question ID
    private String type;               // choice, fill-in, etc.
    private String studentResponse;
    private String correctResponses;
    private String result;             // correct / incorrect
    private String description;

    private LocalDateTime createdAt;
}
