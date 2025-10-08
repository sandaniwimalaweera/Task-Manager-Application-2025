package com.taskmanager.task_manager_backend.service;

import com.taskmanager.task_manager_backend.exception.TaskNotFoundException;
import com.taskmanager.task_manager_backend.model.Notification;
import com.taskmanager.task_manager_backend.model.NotificationType;
import com.taskmanager.task_manager_backend.model.NotificationPriority;
import com.taskmanager.task_manager_backend.model.Task;
import com.taskmanager.task_manager_backend.model.TaskPriority;
import com.taskmanager.task_manager_backend.model.TaskStatus;
import com.taskmanager.task_manager_backend.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private NotificationService notificationService;


    // Get all tasks for a specific user
    public List<Task> getTasksByUsername(String username) {
        return taskRepository.findByUsername(username);
    }

    // Get a specific task by ID and username (for security)
    public Optional<Task> getTaskByIdAndUsername(String id, String username) {
        return taskRepository.findByIdAndUsername(id, username);
    }

    // Create a new task
    @Transactional
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

        Task savedTask = taskRepository.save(task);
        log.info("Task created with ID: {} for user: {}", savedTask.getId(), savedTask.getUsername());

        // Create notification for new task if it's high priority or due soon
        createTaskCreatedNotification(savedTask);

        return savedTask;
    }

    // Update an existing task
    @Transactional
    public Task updateTask(Task task) {
        // Verify task exists
        Optional<Task> existingTaskOpt = taskRepository.findById(task.getId());
        if (existingTaskOpt.isEmpty()) {
            throw new TaskNotFoundException("Task with ID " + task.getId() + " not found");
        }

        Task existingTask = existingTaskOpt.get();

        // Store old status for notification comparison
        TaskStatus oldStatus = existingTask.getStatus();

        task.setUpdatedAt(LocalDateTime.now());

        // Keep the original creation date
        task.setCreatedAt(existingTask.getCreatedAt());

        Task updatedTask = taskRepository.save(task);
        log.info("Task updated with ID: {} for user: {}", updatedTask.getId(), updatedTask.getUsername());

        // Create notifications for status changes
        if (!oldStatus.equals(updatedTask.getStatus())) {
            createTaskStatusChangeNotification(updatedTask, oldStatus);
        }

        return updatedTask;
    }

    // Delete a task
    @Transactional
    public void deleteTask(String id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Task with ID " + id + " not found");
        }

        // Delete associated notifications first
        notificationService.deleteNotificationsByTaskId(id);

        taskRepository.deleteById(id);
        log.info("Task deleted with ID: {}", id);
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
    @Transactional
    public Task updateTaskStatus(String id, TaskStatus status) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (taskOptional.isEmpty()) {
            throw new TaskNotFoundException("Task with ID " + id + " not found");
        }

        Task task = taskOptional.get();
        TaskStatus oldStatus = task.getStatus();

        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(task);
        log.info("Task status updated for ID: {} - New status: {}", id, status);

        // Create notification for status change
        createTaskStatusChangeNotification(updatedTask, oldStatus);

        return updatedTask;
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

    // ==================== NOTIFICATION METHODS ====================

    /**
     * Check for overdue and due soon tasks and create notifications
     * This method is called by the scheduler
     */
    @Transactional
    public void checkAndNotifyDueTasks() {
        List<Task> allTasks = taskRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);

        log.info("Checking {} tasks for due date notifications", allTasks.size());

        int overdueCount = 0;
        int dueSoonCount = 0;

        for (Task task : allTasks) {
            // Skip completed tasks or tasks without due dates
            if (task.getStatus() == TaskStatus.COMPLETED || task.getDueDate() == null) {
                continue;
            }

            LocalDateTime dueDate = task.getDueDate();

            // Check if task is overdue
            if (dueDate.isBefore(now)) {
                createOverdueNotification(task);
                overdueCount++;
            }
            // Check if task is due today or tomorrow
            else if (dueDate.isBefore(tomorrow)) {
                createDueSoonNotification(task);
                dueSoonCount++;
            }
        }

        log.info("Created {} overdue and {} due soon notifications", overdueCount, dueSoonCount);
    }

    // ==================== PRIVATE NOTIFICATION HELPER METHODS ====================

    /**
     * Create notification when a task is created
     */
    private void createTaskCreatedNotification(Task task) {
        // Only notify for high priority tasks or tasks due soon
        boolean isHighPriority = task.getPriority() == TaskPriority.HIGH;
        boolean isDueSoon = task.getDueDate() != null &&
                ChronoUnit.HOURS.between(LocalDateTime.now(), task.getDueDate()) <= 72; // 3 days

        if (!isHighPriority && !isDueSoon) {
            return;
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("taskTitle", task.getTitle());
        metadata.put("taskPriority", task.getPriority().toString());
        if (task.getDueDate() != null) {
            metadata.put("dueDate", task.getDueDate().toString());
        }

        NotificationPriority priority = isHighPriority ?
                NotificationPriority.HIGH :
                NotificationPriority.MEDIUM;

        String message = isHighPriority ?
                "New high priority task created" :
                "New task created - Due soon";

        notificationService.createTaskNotification(
                task.getUsername(),
                task.getId(),
                NotificationType.SYSTEM,
                priority,
                "Task Created: " + task.getTitle(),
                message,
                metadata
        );

        log.info("Created task creation notification for task: {}", task.getId());
    }

    /**
     * Create notification when task status changes
     */
    private void createTaskStatusChangeNotification(Task task, TaskStatus oldStatus) {
        // Only create notification if task is completed
        if (task.getStatus() == TaskStatus.COMPLETED && oldStatus != TaskStatus.COMPLETED) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("taskTitle", task.getTitle());
            metadata.put("taskPriority", task.getPriority().toString());
            metadata.put("previousStatus", oldStatus.toString());

            notificationService.createTaskNotification(
                    task.getUsername(),
                    task.getId(),
                    NotificationType.TASK_COMPLETED,
                    NotificationPriority.LOW,
                    "Task Completed! 🎉",
                    "You've completed: " + task.getTitle(),
                    metadata
            );

            log.info("Created task completion notification for task: {}", task.getId());
        }
    }

    /**
     * Create notification for overdue tasks
     */
    private void createOverdueNotification(Task task) {
        LocalDateTime dueDate = task.getDueDate();
        long hoursOverdue = ChronoUnit.HOURS.between(dueDate, LocalDateTime.now());
        long daysOverdue = hoursOverdue / 24;

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("taskTitle", task.getTitle());
        metadata.put("taskPriority", task.getPriority().toString());
        metadata.put("dueDate", dueDate.toString());
        metadata.put("hoursOverdue", hoursOverdue);
        metadata.put("daysOverdue", daysOverdue);

        String message;
        if (daysOverdue >= 1) {
            message = String.format("%s is overdue by %d day(s)", task.getTitle(), daysOverdue);
        } else {
            message = String.format("%s is overdue by %d hour(s)", task.getTitle(), hoursOverdue);
        }

        notificationService.createTaskNotification(
                task.getUsername(),
                task.getId(),
                NotificationType.TASK_OVERDUE,
                NotificationPriority.URGENT,
                "Task Overdue!",
                message,
                metadata
        );

        log.debug("Created overdue notification for task: {} (overdue by {} hours)",
                task.getId(), hoursOverdue);
    }

    /**
     * Create notification for tasks due soon
     */
    private void createDueSoonNotification(Task task) {
        LocalDateTime dueDate = task.getDueDate();
        long hoursUntilDue = ChronoUnit.HOURS.between(LocalDateTime.now(), dueDate);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("taskTitle", task.getTitle());
        metadata.put("taskPriority", task.getPriority().toString());
        metadata.put("dueDate", dueDate.toString());
        metadata.put("hoursUntilDue", hoursUntilDue);

        String message;
        if (hoursUntilDue <= 24) {
            message = task.getTitle() + " is due today!";
        } else {
            message = task.getTitle() + " is due tomorrow";
        }

        notificationService.createTaskNotification(
                task.getUsername(),
                task.getId(),
                NotificationType.TASK_DUE_SOON,
                NotificationPriority.HIGH,
                "Task Due Soon",
                message,
                metadata
        );

        log.debug("Created due soon notification for task: {} (due in {} hours)",
                task.getId(), hoursUntilDue);
    }
}