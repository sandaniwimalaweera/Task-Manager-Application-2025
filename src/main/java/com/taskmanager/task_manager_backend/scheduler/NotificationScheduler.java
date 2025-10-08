package com.taskmanager.task_manager_backend.scheduler;

import com.taskmanager.task_manager_backend.service.NotificationService;
import com.taskmanager.task_manager_backend.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

    @Autowired
    private TaskService taskService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Check for due and overdue tasks every hour
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkDueTasks() {
        log.info("=== Starting scheduled task: Checking for due and overdue tasks ===");

        try {
            taskService.checkAndNotifyDueTasks();
            log.info("=== Successfully completed due tasks check ===");
        } catch (Exception e) {
            log.error("=== Error checking due tasks ===", e);
        }
    }

    /**
     * Clean up old notifications every day at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldNotifications() {
        log.info("=== Starting scheduled task: Cleaning up old notifications ===");

        try {
            notificationService.cleanupOldNotifications();
            log.info("=== Successfully completed notification cleanup ===");
        } catch (Exception e) {
            log.error("=== Error cleaning up notifications ===", e);
        }
    }
}