package com.tickets.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/auth")
    public ResponseEntity<Map<String, Object>> authServiceFallback() {
        return buildFallbackResponse("Authentication Service is temporarily unavailable. Please try again later.");
    }

    @RequestMapping("/user")
    public ResponseEntity<Map<String, Object>> userServiceFallback() {
        return buildFallbackResponse("User Service is temporarily unavailable. Please try again later.");
    }

    @RequestMapping("/event")
    public ResponseEntity<Map<String, Object>> eventServiceFallback() {
        return buildFallbackResponse("Event Service is temporarily unavailable. Please try again later.");
    }

    @RequestMapping("/ticket")
    public ResponseEntity<Map<String, Object>> ticketServiceFallback() {
        return buildFallbackResponse("Ticket Service is temporarily unavailable. Please try again later.");
    }

    @RequestMapping("/payment")
    public ResponseEntity<Map<String, Object>> paymentServiceFallback() {
        return buildFallbackResponse("Payment Service is temporarily unavailable. Please try again later.");
    }

    private ResponseEntity<Map<String, Object>> buildFallbackResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", message);
        response.put("suggestion", "Our team has been notified. Please try again in a few moments.");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}
