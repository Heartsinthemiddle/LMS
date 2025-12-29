package com.lms.dto.request;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class VideoMessage {
    @Id
    @GeneratedValue
    private Long id;
    private String learnerId;
    private String courseId;
    private String videoId;
    private String message;
    private String parentPhoneNumber;
    private LocalDateTime sentAt = LocalDateTime.now();
}
