package com.lms.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Value("${twilio.phoneNumber}")
    private String fromPhoneNumber;

    public void sendSms(String toPhoneNumber, String messageBody) {
        Message.creator(
                new PhoneNumber(toPhoneNumber),  // To
                new PhoneNumber(fromPhoneNumber), // From (Twilio number)
                messageBody
        ).create();

        System.out.println("SMS sent to " + toPhoneNumber + ": " + messageBody);
    }
}

