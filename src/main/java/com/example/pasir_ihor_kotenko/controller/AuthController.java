package com.example.pasir_ihor_kotenko.controller;

import com.example.pasir_ihor_kotenko.dto.LoginDto;
import com.example.pasir_ihor_kotenko.dto.UserDto;
import com.example.pasir_ihor_kotenko.model.User;
import com.example.pasir_ihor_kotenko.service.CurrentUserService;
import com.example.pasir_ihor_kotenko.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final CurrentUserService currentUserService;

    public AuthController(UserService userService, CurrentUserService currentUserService) {
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody UserDto dto) {
        userService.register(dto);
        return ResponseEntity.ok(Map.of("message", "Użytkownik zarejestrowany pomyślnie"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginDto dto) {
        String token = userService.login(dto);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(Map.of("id", user.getId(), "email", user.getEmail()));
    }
}
