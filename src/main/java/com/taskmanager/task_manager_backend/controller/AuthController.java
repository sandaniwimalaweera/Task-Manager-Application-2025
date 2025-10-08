package com.taskmanager.task_manager_backend.controller;

import com.taskmanager.task_manager_backend.dto.LoginRequest;
import com.taskmanager.task_manager_backend.dto.LoginResponse;
import com.taskmanager.task_manager_backend.dto.UserProfileDto;
import com.taskmanager.task_manager_backend.service.AuthenticationService;
import com.taskmanager.task_manager_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authenticationService.authenticate(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserProfileDto userProfileDto) {
        userService.registerUser(userProfileDto);

        // Return JSON response instead of plain text
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("username", userProfileDto.getUsername());
        response.put("success", true);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        // With JWT, logout is typically handled client-side by removing the token
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        response.put("success", true);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken() {
        // This endpoint can be used to check if the current token is valid
        // Spring Security will handle the validation through the JWT filter
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Token is valid");
        response.put("valid", true);

        return ResponseEntity.ok(response);
    }
}