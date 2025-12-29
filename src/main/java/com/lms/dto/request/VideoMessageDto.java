package com.lms.dto.request;

import lombok.Data;

@Data
public class VideoMessageDto {
    private String learnerId;
    private String courseId;
    private String videoId;
    private String message;
    // getters and setters
}
