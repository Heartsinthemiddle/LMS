package com.lms.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "scorm_tracking")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ScormTracking extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long courseId;

    private String lessonStatus; // completed, incomplete
    private String lessonLocation;
    private String scoreRaw;

    @Lob
    private String suspendData;

    private String totalTime;

}
