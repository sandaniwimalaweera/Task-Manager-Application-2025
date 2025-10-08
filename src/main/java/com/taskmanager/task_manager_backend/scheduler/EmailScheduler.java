package com.taskmanager.task_manager_backend.scheduler;

import com.taskmanager.task_manager_backend.model.Task;
import com.taskmanager.task_manager_backend.model.TaskStatus;
import com.taskmanager.task_manager_backend.model.User;
import com.taskmanager.task_manager_backend.repository.TaskRepository;
import com.taskmanager.task_manager_backend.repository.UserRepository;
import com.taskmanager.task_manager_backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class EmailScheduler {

    private static final Logger log = LoggerFactory.getLogger(EmailScheduler.class);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // Run every day at 9 AM
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendDailyTaskReminders() {
        log.info("Starting daily task reminder email job");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        LocalDateTime dayAfterTomorrow = now.plusDays(2);

        // Get all tasks
        List<Task> allTasks = taskRepository.findAll();

        int emailsSent = 0;

        for (Task task : allTasks) {
            // Skip completed tasks or tasks without due dates
            if (task.getStatus() == TaskStatus.COMPLETED || task.getDueDate() == null) {
                continue;
            }

            LocalDateTime dueDate = task.getDueDate();

            // Check if task is due tomorrow
            if (dueDate.isAfter(now) && dueDate.isBefore(dayAfterTomorrow)) {
                Optional<User> userOpt = userRepository.findByUsername(task.getUsername());

                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    emailService.sendTaskDueReminderEmail(user.getEmail(), task);
                    emailsSent++;
                    log.info("Sent task reminder email to {} for task: {}", user.getEmail(), task.getTitle());
                }
            }

            // Optional: Send overdue task emails
            if (dueDate.isBefore(now)) {
                Optional<User> userOpt = userRepository.findByUsername(task.getUsername());

                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    emailService.sendTaskOverdueEmail(user.getEmail(), task);
                    emailsSent++;
                    log.info("Sent overdue task email to {} for task: {}", user.getEmail(), task.getTitle());
                }
            }
        }

        log.info("Daily task reminder email job completed. Sent {} emails", emailsSent);
    }

    // Optional: Run every hour to check for overdue tasks
    @Scheduled(cron = "0 0 * * * ?")
    public void sendOverdueTaskReminders() {
        log.info("Checking for overdue tasks");

        LocalDateTime now = LocalDateTime.now();
        List<Task> allTasks = taskRepository.findAll();

        int emailsSent = 0;

        for (Task task : allTasks) {
            if (task.getStatus() != TaskStatus.COMPLETED &&
                    task.getDueDate() != null &&
                    task.getDueDate().isBefore(now)) {

                Optional<User> userOpt = userRepository.findByUsername(task.getUsername());

                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    emailService.sendTaskOverdueEmail(user.getEmail(), task);
                    emailsSent++;
                }
            }
        }

        log.info("Overdue task check completed. Sent {} emails", emailsSent);
    }
}