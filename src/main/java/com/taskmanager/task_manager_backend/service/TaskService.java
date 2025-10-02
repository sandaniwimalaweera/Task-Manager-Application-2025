package com.taskmanager.task_manager_backend.service;

import com.taskmanager.task_manager_backend.exception.TaskNotFoundException;
import com.taskmanager.task_manager_backend.model.Task;
import com.taskmanager.task_manager_backend.model.TaskPriority;
import com.taskmanager.task_manager_backend.model.TaskStatus;
import com.taskmanager.task_manager_backend.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    // Get all tasks for a specific user
    public List<Task> getTasksByUsername(String username) {
        return taskRepository.findByUsername(username);
    }

    // Get a specific task by ID and username (for security)
    public Optional<Task> getTaskByIdAndUsername(String id, String username) {
        return taskRepository.findByIdAndUsername(id, username);
    }

    // Create a new task
    public Task createTask(Task task) {
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        // Set default status if not provided
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }

        // Set default priority if not provided
        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.MEDIUM);
        }

        return taskRepository.save(task);
    }

    // Update an existing task
    public Task updateTask(Task task) {
        // Verify task exists
        Optional<Task> existingTask = taskRepository.findById(task.getId());
        if (existingTask.isEmpty()) {
            throw new TaskNotFoundException("Task with ID " + task.getId() + " not found");
        }

        task.setUpdatedAt(LocalDateTime.now());

        // Keep the original creation date
        task.setCreatedAt(existingTask.get().getCreatedAt());

        return taskRepository.save(task);
    }

    // Delete a task
    public void deleteTask(String id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Task with ID " + id + " not found");
        }
        taskRepository.deleteById(id);
    }

    // Get tasks by status and username
    public List<Task> getTasksByStatusAndUsername(TaskStatus status, String username) {
        return taskRepository.findByStatusAndUsername(status, username);
    }

    // Get tasks by priority and username
    public List<Task> getTasksByPriorityAndUsername(TaskPriority priority, String username) {
        return taskRepository.findByPriorityAndUsername(priority, username);
    }

    // Search tasks by keyword in title or description
    public List<Task> searchTasksByKeyword(String keyword, String username) {
        return taskRepository.findByUsernameAndTitleContainingIgnoreCaseOrUsernameAndDescriptionContainingIgnoreCase(
                username, keyword, username, keyword
        );
    }

    // Update only the status of a task
    public Task updateTaskStatus(String id, TaskStatus status) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (taskOptional.isEmpty()) {
            throw new TaskNotFoundException("Task with ID " + id + " not found");
        }

        Task task = taskOptional.get();
        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    // Get count of tasks by status for a user (for dashboard)
    public long countTasksByStatusAndUsername(TaskStatus status, String username) {
        return taskRepository.countByStatusAndUsername(status, username);
    }

    // Get count of tasks by priority for a user (for dashboard)
    public long countTasksByPriorityAndUsername(TaskPriority priority, String username) {
        return taskRepository.countByPriorityAndUsername(priority, username);
    }

    // Get count of overdue tasks for a user (for dashboard)
    public long countOverdueTasksByUsername(String username) {
        LocalDateTime now = LocalDateTime.now();
        return taskRepository.countByUsernameAndDueDateBeforeAndStatusNot(username, now, TaskStatus.COMPLETED);
    }

    // Get total count of tasks for a user
    public long countTasksByUsername(String username) {
        return taskRepository.countByUsername(username);
    }
}