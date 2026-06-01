package com.example.pasir_ihor_kotenko.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfoController {

    @GetMapping("/api/info")
    public Map<String, String> info() {
        return Map.of(
                "appName", "My spend app",
                "version", "1.0",
                "message", "hello from me!"
        );
    }
}
