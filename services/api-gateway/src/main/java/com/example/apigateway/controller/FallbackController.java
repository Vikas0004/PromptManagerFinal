package com.example.apigateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    private static final Logger logger = LoggerFactory.getLogger(FallbackController.class);

    @GetMapping("/analytics")
    public ResponseEntity<String> inventoryFallback() {
        logger.warn("Analytics Service is unavailable, fallback triggered.");
        return new ResponseEntity<>("Analytics Service is currently unavailable. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @GetMapping("/prompts")
    public ResponseEntity<String> paymentFallback() {
        logger.warn("Prompt Service is unavailable, fallback triggered.");
        return new ResponseEntity<>("Prompt Service is currently unavailable. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
    }
}
