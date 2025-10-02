package com.taskmanager.task_manager_backend.controller;

import com.taskmanager.task_manager_backend.dto.UserProfileDto;
import com.taskmanager.task_manager_backend.model.User;
import com.taskmanager.task_manager_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getUserProfile(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);

        if (user.isPresent()) {
            UserProfileDto userProfile = new UserProfileDto(user.get());
            return ResponseEntity.ok(userProfile);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateUserProfile(@Valid @RequestBody UserProfileDto userProfileDto,
                                                            Authentication authentication) {
        String username = authentication.getName();

        // Ensure the user can only update their own profile
        if (!username.equals(userProfileDto.getUsername())) {
            return ResponseEntity.status(403).build();
        }

        User updatedUser = userService.updateUserProfile(userProfileDto);
        UserProfileDto responseDto = new UserProfileDto(updatedUser);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestParam String currentPassword,
                                                 @RequestParam String newPassword,
                                                 Authentication authentication) {
        String username = authentication.getName();

        boolean success = userService.changePassword(username, currentPassword, newPassword);

        if (success) {
            return ResponseEntity.ok("Password changed successfully");
        } else {
            return ResponseEntity.badRequest().body("Current password is incorrect");
        }
    }

    @DeleteMapping("/account")
    public ResponseEntity<String> deleteAccount(Authentication authentication) {
        String username = authentication.getName();
        userService.deleteUser(username);
        return ResponseEntity.ok("Account deleted successfully");
    }

    @GetMapping("/dashboard")
    public ResponseEntity<UserService.DashboardStats> getDashboardStats(Authentication authentication) {
        String username = authentication.getName();
        UserService.DashboardStats stats = userService.getUserDashboardStats(username);
        return ResponseEntity.ok(stats);
    }
}