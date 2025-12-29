package com.lms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class TestController {
    @GetMapping("/course")
    public String coursePage() {
        return "course"; // maps to templates/course.html
    }
}
