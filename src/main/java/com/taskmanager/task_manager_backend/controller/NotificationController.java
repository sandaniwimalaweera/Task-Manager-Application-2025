package com.taskmanager.task_manager_backend.controller;

import com.taskmanager.task_manager_backend.model.Notification;
import com.taskmanager.task_manager_backend.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "true") boolean includeRead,
            @RequestParam(defaultValue = "false") boolean includeArchived) {

        String username = authentication.getName();
        log.info("User {} fetching notifications", username);

        List<Notification> notifications = notificationService.getAllNotifications(
                username, includeRead, includeArchived);

        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(Authentication authentication) {
        String username = authentication.getName();
        log.info("User {} fetching unread notifications", username);

        List<Notification> notifications = notificationService.getUnreadNotifications(username);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        String username = authentication.getName();
        long count = notificationService.getUnreadCount(username);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getNotificationStats(Authentication authentication) {
        String username = authentication.getName();
        log.info("User {} fetching notification statistics", username);

        Map<String, Object> stats = notificationService.getNotificationStats(username);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(
            Authentication authentication,
            @PathVariable String id) {

        String username = authentication.getName();

        return notificationService.getNotificationById(id)
                .filter(notification -> notification.getUsername().equals(username))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Notification> createNotification(
            Authentication authentication,
            @RequestBody Notification notification) {

        String username = authentication.getName();
        notification.setUsername(username);

        log.info("Creating notification for user: {}", username);

        Notification createdNotification = notificationService.createNotification(notification);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNotification);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(
            Authentication authentication,
            @PathVariable String id) {

        String username = authentication.getName();

        return notificationService.getNotificationById(id)
                .filter(notification -> notification.getUsername().equals(username))
                .map(notification -> {
                    Notification updatedNotification = notificationService.markAsRead(id);
                    log.info("User {} marked notification {} as read", username, id);
                    return ResponseEntity.ok(updatedNotification);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/mark-read")
    public ResponseEntity<Void> markMultipleAsRead(
            Authentication authentication,
            @RequestBody Map<String, List<String>> request) {

        String username = authentication.getName();
        List<String> ids = request.get("ids");

        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        log.info("User {} marking {} notifications as read", username, ids.size());
        notificationService.markMultipleAsRead(ids);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        String username = authentication.getName();
        log.info("User {} marking all notifications as read", username);

        notificationService.markAllAsRead(username);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<Void> archiveNotification(
            Authentication authentication,
            @PathVariable String id) {

        String username = authentication.getName();

        return notificationService.getNotificationById(id)
                .filter(notification -> notification.getUsername().equals(username))
                .map(notification -> {
                    notificationService.archiveNotification(id);
                    log.info("User {} archived notification {}", username, id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            Authentication authentication,
            @PathVariable String id) {

        String username = authentication.getName();

        return notificationService.getNotificationById(id)
                .filter(notification -> notification.getUsername().equals(username))
                .map(notification -> {
                    notificationService.deleteNotification(id);
                    log.info("User {} deleted notification {}", username, id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/read")
    public ResponseEntity<Void> deleteAllRead(Authentication authentication) {
        String username = authentication.getName();
        log.info("User {} deleting all read notifications", username);

        notificationService.deleteAllRead(username);
        return ResponseEntity.ok().build();
    }
}