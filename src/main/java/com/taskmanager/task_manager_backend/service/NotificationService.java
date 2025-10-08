package com.taskmanager.task_manager_backend.service;

import com.taskmanager.task_manager_backend.model.Notification;
import com.taskmanager.task_manager_backend.model.NotificationType;
import com.taskmanager.task_manager_backend.model.NotificationPriority;
import com.taskmanager.task_manager_backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    public List<Notification> getAllNotifications(String username, boolean includeRead, boolean includeArchived) {
        if (!includeArchived) {
            if (!includeRead) {
                return notificationRepository.findByUsernameAndReadFalseAndArchivedFalseOrderByCreatedAtDesc(username);
            }
            return notificationRepository.findByUsernameAndArchivedFalseOrderByCreatedAtDesc(username);
        }
        return notificationRepository.findByUsernameOrderByCreatedAtDesc(username);
    }

    public List<Notification> getUnreadNotifications(String username) {
        return notificationRepository.findByUsernameAndReadFalseOrderByCreatedAtDesc(username);
    }

    public Optional<Notification> getNotificationById(String id) {
        return notificationRepository.findById(id);
    }

    public long getUnreadCount(String username) {
        return notificationRepository.countByUsernameAndReadFalse(username);
    }

    public Map<String, Object> getNotificationStats(String username) {
        List<Notification> allNotifications = notificationRepository.findByUsernameOrderByCreatedAtDesc(username);

        long total = allNotifications.size();
        long unread = allNotifications.stream().filter(n -> !n.isRead()).count();

        Map<NotificationType, Long> byType = allNotifications.stream()
                .collect(Collectors.groupingBy(Notification::getType, Collectors.counting()));

        Map<NotificationPriority, Long> byPriority = allNotifications.stream()
                .collect(Collectors.groupingBy(Notification::getPriority, Collectors.counting()));

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("unread", unread);
        stats.put("byType", byType);
        stats.put("byPriority", byPriority);

        return stats;
    }

    public Notification createNotification(Notification notification) {
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notification.setArchived(false);

        log.info("Creating notification for user: {} - Type: {}",
                notification.getUsername(), notification.getType());

        return notificationRepository.save(notification);
    }

    public Notification createTaskNotification(
            String username,
            String taskId,
            NotificationType type,
            NotificationPriority priority,
            String title,
            String message,
            Map<String, Object> metadata) {

        Notification notification = new Notification();
        notification.setUsername(username);
        notification.setTaskId(taskId);
        notification.setType(type);
        notification.setPriority(priority);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setMetadata(metadata);
        notification.setActionUrl("/tasks?taskId=" + taskId);

        return createNotification(notification);
    }

    public Notification markAsRead(String id) {
        Optional<Notification> optionalNotification = notificationRepository.findById(id);

        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            return notificationRepository.save(notification);
        }

        throw new RuntimeException("Notification not found with id: " + id);
    }

    @Transactional
    public void markMultipleAsRead(List<String> ids) {
        for (String id : ids) {
            try {
                markAsRead(id);
            } catch (Exception e) {
                log.error("Error marking notification as read: {}", id, e);
            }
        }
    }

    @Transactional
    public void markAllAsRead(String username) {
        List<Notification> unreadNotifications = notificationRepository
                .findByUsernameAndReadFalseOrderByCreatedAtDesc(username);

        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }

        log.info("Marked {} notifications as read for user: {}", unreadNotifications.size(), username);
    }

    public Notification archiveNotification(String id) {
        Optional<Notification> optionalNotification = notificationRepository.findById(id);

        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            notification.setArchived(true);
            return notificationRepository.save(notification);
        }

        throw new RuntimeException("Notification not found with id: " + id);
    }

    public void deleteNotification(String id) {
        notificationRepository.deleteById(id);
        log.info("Deleted notification: {}", id);
    }

    @Transactional
    public void deleteAllRead(String username) {
        notificationRepository.deleteByUsernameAndReadTrue(username);
        log.info("Deleted all read notifications for user: {}", username);
    }

    @Transactional
    public void deleteNotificationsByTaskId(String taskId) {
        notificationRepository.deleteByTaskId(taskId);
        log.info("Deleted notifications for task: {}", taskId);
    }

    @Transactional
    public void cleanupOldNotifications() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Notification> oldNotifications = notificationRepository.findByCreatedAtBefore(thirtyDaysAgo);

        int deletedCount = 0;
        for (Notification notification : oldNotifications) {
            if (notification.isRead()) {
                notificationRepository.delete(notification);
                deletedCount++;
            }
        }

        log.info("Cleaned up {} old notifications", deletedCount);
    }
}