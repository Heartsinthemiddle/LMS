//package com.lms.controller;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/v1/scorm")
//public class ScormStatementController {
//
//    @PostMapping("/statements")
//    public ResponseEntity<Void> receiveStatement(@RequestBody Map<String, Object> stmt) {
//
//        String verb = ((Map<?, ?>) stmt.get("verb")).get("id").toString();
//
//        if (verb.contains("answered")) {
//            saveAnswer(stmt);
//        }
//
//        if (verb.contains("completed")) {
//            markCompleted(stmt);
//        }
//
//        return ResponseEntity.ok().build();
//    }
//}
