package com.example.pasir_ihor_kotenko.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String root() {
        return "Created by Kotenko Ihor. Available endpoints: /api/test, /api/info, /api/transactions, /api/auth";
    }

    @GetMapping("/api/test")
    public String test() {
        return "Testing message!";
    }
}
