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
    public ResponseEntity<String> register(@Valid @RequestBody UserProfileDto userProfileDto) {
        userService.registerUser(userProfileDto);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // With JWT, logout is typically handled client-side by removing the token
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validateToken() {
        // This endpoint can be used to check if the current token is valid
        // Spring Security will handle the validation through the JWT filter
        return ResponseEntity.ok("Token is valid");
    }
}