package com.taskmanager.task_manager_backend.service;

import com.taskmanager.task_manager_backend.dto.UserProfileDto;
import com.taskmanager.task_manager_backend.exception.UserAlreadyExistsException;
import com.taskmanager.task_manager_backend.exception.UserNotFoundException;
import com.taskmanager.task_manager_backend.model.TaskPriority;
import com.taskmanager.task_manager_backend.model.TaskStatus;
import com.taskmanager.task_manager_backend.model.User;
import com.taskmanager.task_manager_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TaskService taskService;

    // Find user by username
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Find user by email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Register a new user
    public User registerUser(UserProfileDto userProfileDto) {
        // Check if username already exists
        if (userRepository.existsByUsername(userProfileDto.getUsername())) {
            throw new UserAlreadyExistsException("Username '" + userProfileDto.getUsername() + "' is already taken");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(userProfileDto.getEmail())) {
            throw new UserAlreadyExistsException("Email '" + userProfileDto.getEmail() + "' is already registered");
        }

        // Create new user
        User user = new User();
        user.setUsername(userProfileDto.getUsername());
        user.setEmail(userProfileDto.getEmail());
        user.setFirstName(userProfileDto.getFirstName());
        user.setLastName(userProfileDto.getLastName());
        user.setPassword(passwordEncoder.encode(userProfileDto.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    // Update user profile
    public User updateUserProfile(UserProfileDto userProfileDto) {
        Optional<User> userOptional = userRepository.findByUsername(userProfileDto.getUsername());
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User not found: " + userProfileDto.getUsername());
        }

        User user = userOptional.get();

        // Check if email is being changed and if it's already taken by another user
        if (!user.getEmail().equals(userProfileDto.getEmail())) {
            if (userRepository.existsByEmail(userProfileDto.getEmail())) {
                throw new UserAlreadyExistsException("Email '" + userProfileDto.getEmail() + "' is already registered");
            }
            user.setEmail(userProfileDto.getEmail());
        }

        // Update other fields
        user.setFirstName(userProfileDto.getFirstName());
        user.setLastName(userProfileDto.getLastName());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    // Change user password
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User not found: " + username);
        }

        User user = userOptional.get();

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false; // Current password is incorrect
        }

        // Validate new password
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return true;
    }

    // Delete user account
    public void deleteUser(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User not found: " + username);
        }

        userRepository.delete(userOptional.get());
    }

    // Get dashboard statistics for a user
    public DashboardStats getUserDashboardStats(String username) {
        // Verify user exists
        if (!userRepository.existsByUsername(username)) {
            throw new UserNotFoundException("User not found: " + username);
        }

        // Get task counts
        int totalTasks = (int) taskService.countTasksByUsername(username);
        int completedTasks = (int) taskService.countTasksByStatusAndUsername(TaskStatus.COMPLETED, username);
        int todoTasks = (int) taskService.countTasksByStatusAndUsername(TaskStatus.TODO, username);
        int inProgressTasks = (int) taskService.countTasksByStatusAndUsername(TaskStatus.IN_PROGRESS, username);
        int pendingTasks = todoTasks + inProgressTasks;
        int overdueTasks = (int) taskService.countOverdueTasksByUsername(username);
        int highPriorityTasks = (int) taskService.countTasksByPriorityAndUsername(TaskPriority.HIGH, username);

        return new DashboardStats(totalTasks, completedTasks, pendingTasks, overdueTasks, highPriorityTasks);
    }

    // Check if user exists by username
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    // Check if user exists by email
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Inner class for dashboard statistics
    public static class DashboardStats {
        private int totalTasks;
        private int completedTasks;
        private int pendingTasks;
        private int overdueTasks;
        private int highPriorityTasks;

        public DashboardStats(int totalTasks, int completedTasks, int pendingTasks,
                              int overdueTasks, int highPriorityTasks) {
            this.totalTasks = totalTasks;
            this.completedTasks = completedTasks;
            this.pendingTasks = pendingTasks;
            this.overdueTasks = overdueTasks;
            this.highPriorityTasks = highPriorityTasks;
        }

        // Getters and Setters
        public int getTotalTasks() { return totalTasks; }
        public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

        public int getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

        public int getPendingTasks() { return pendingTasks; }
        public void setPendingTasks(int pendingTasks) { this.pendingTasks = pendingTasks; }

        public int getOverdueTasks() { return overdueTasks; }
        public void setOverdueTasks(int overdueTasks) { this.overdueTasks = overdueTasks; }

        public int getHighPriorityTasks() { return highPriorityTasks; }
        public void setHighPriorityTasks(int highPriorityTasks) { this.highPriorityTasks = highPriorityTasks; }
    }
}