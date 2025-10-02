package com.taskmanager.task_manager_backend.controller;

import com.taskmanager.task_manager_backend.model.Task;
import com.taskmanager.task_manager_backend.model.TaskPriority;
import com.taskmanager.task_manager_backend.model.TaskStatus;
import com.taskmanager.task_manager_backend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = "http://localhost:4200")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(Authentication authentication) {
        String username = authentication.getName();
        List<Task> tasks = taskService.getTasksByUsername(username);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable String id, Authentication authentication) {
        String username = authentication.getName();
        Optional<Task> task = taskService.getTaskByIdAndUsername(id, username);

        if (task.isPresent()) {
            return ResponseEntity.ok(task.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task, Authentication authentication) {
        String username = authentication.getName();
        task.setUsername(username); // Ensure task belongs to authenticated user
        Task savedTask = taskService.createTask(task);
        return ResponseEntity.ok(savedTask);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable String id,
                                           @Valid @RequestBody Task task,
                                           Authentication authentication) {
        String username = authentication.getName();

        // Verify the task belongs to the authenticated user
        Optional<Task> existingTask = taskService.getTaskByIdAndUsername(id, username);
        if (existingTask.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        task.setId(id);
        task.setUsername(username);
        Task updatedTask = taskService.updateTask(task);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id, Authentication authentication) {
        String username = authentication.getName();

        // Verify the task belongs to the authenticated user
        Optional<Task> existingTask = taskService.getTaskByIdAndUsername(id, username);
        if (existingTask.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Task>> getTasksByStatus(@PathVariable TaskStatus status,
                                                       Authentication authentication) {
        String username = authentication.getName();
        List<Task> tasks = taskService.getTasksByStatusAndUsername(status, username);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<Task>> getTasksByPriority(@PathVariable TaskPriority priority,
                                                         Authentication authentication) {
        String username = authentication.getName();
        List<Task> tasks = taskService.getTasksByPriorityAndUsername(priority, username);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Task>> searchTasks(@RequestParam String keyword,
                                                  Authentication authentication) {
        String username = authentication.getName();
        List<Task> tasks = taskService.searchTasksByKeyword(keyword, username);
        return ResponseEntity.ok(tasks);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> updateTaskStatus(@PathVariable String id,
                                                 @RequestParam TaskStatus status,
                                                 Authentication authentication) {
        String username = authentication.getName();

        Optional<Task> existingTask = taskService.getTaskByIdAndUsername(id, username);
        if (existingTask.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Task updatedTask = taskService.updateTaskStatus(id, status);
        return ResponseEntity.ok(updatedTask);
    }
}