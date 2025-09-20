package com.mahabaleshwermart.productservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;

/**
 * Simple test controller to debug POST mapping issues
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestController {

    @PostConstruct
    public void init() {
        log.info("TestController initialized successfully!");
    }

    @GetMapping("/hello")
    public ResponseEntity<String> getHello() {
        return ResponseEntity.ok("GET Hello World!");
    }

    @PostMapping("/hello")
    public ResponseEntity<String> postHello(@RequestBody String message) {
        return ResponseEntity.ok("POST received: " + message);
    }

    @RequestMapping(value = "/hello2", method = RequestMethod.POST)
    public ResponseEntity<String> postHello2(@RequestBody String message) {
        return ResponseEntity.ok("POST2 received: " + message);
    }
}
