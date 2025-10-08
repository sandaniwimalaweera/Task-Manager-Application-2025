package com.taskmanager.task_manager_backend.controller;

import com.taskmanager.task_manager_backend.dto.*;
import com.taskmanager.task_manager_backend.model.User;
import com.taskmanager.task_manager_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/profile")
@CrossOrigin(origins = "*")
public class UserProfileController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile() {
        try {
            String userId = getCurrentUserId();
            UserProfileResponse profile = userService.getProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            System.err.println("Error getting profile: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {

        String userId = getCurrentUserId();
        UserProfileResponse updatedProfile = userService.updateProfile(userId, request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        String userId = getCurrentUserId();
        userService.changePasswordEnhanced(userId, request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-email")
    public ResponseEntity<UserProfileResponse> changeEmail(
            @Valid @RequestBody ChangeEmailRequest request) {

        String userId = getCurrentUserId();
        UserProfileResponse updatedProfile = userService.changeEmail(userId, request);

        return ResponseEntity.ok(updatedProfile);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String identifier = authentication.getName();
        System.out.println("DEBUG: Extracted identifier from JWT: " + identifier);
        System.out.println("DEBUG: Is it an email? " + identifier.contains("@"));

        // Try finding by username first
        Optional<User> userOptional = userService.findByUsername(identifier);

        // If not found by username, try by email
        if (userOptional.isEmpty()) {
            System.out.println("DEBUG: Not found by username, trying email...");
            userOptional = userService.findByEmail(identifier);
        }

        User user = userOptional.orElseThrow(() -> {
            System.err.println("ERROR: User not found for identifier: " + identifier);
            return new RuntimeException("User not found: " + identifier);
        });

        System.out.println("DEBUG: Found user - ID: " + user.getId() + ", Username: " + user.getUsername());
        return user.getId();
    }
}