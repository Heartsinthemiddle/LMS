package com.lms.controller;

import com.lms.dto.request.VideoMessage;
import com.lms.dto.request.VideoMessageDto;
import com.lms.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class SMSMessageController {

    private final NotificationService notificationService;

    public SMSMessageController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/video-message")
    public ResponseEntity<Void> sendVideoMessage(@RequestBody VideoMessageDto dto) {
        // Fetch parent's phone number from DB based on learnerId
        String parentPhone = fetchParentPhone(dto.getLearnerId());

        // Send SMS via Twilio
        notificationService.sendSms(parentPhone, dto.getMessage());

        return ResponseEntity.ok().build();
    }

    private String fetchParentPhone(String learnerId) {
        // Replace with actual DB query to get parent's number
        return "+919667961893";
    }


}

