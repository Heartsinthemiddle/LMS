package com.lms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "scorm_course")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ScormCourse extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String version; // SCORM_1_2 / SCORM_2004
    private String launchUrl;

    private String basePath; // extracted folder path

}

