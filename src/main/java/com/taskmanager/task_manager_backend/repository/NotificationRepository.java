package com.taskmanager.task_manager_backend.repository;

import com.taskmanager.task_manager_backend.model.Notification;
import com.taskmanager.task_manager_backend.model.NotificationType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    // Change from IsRead to Read
    List<Notification> findByUsernameAndReadFalseAndArchivedFalseOrderByCreatedAtDesc(String username);

    List<Notification> findByUsernameAndArchivedFalseOrderByCreatedAtDesc(String username);

    List<Notification> findByUsernameOrderByCreatedAtDesc(String username);

    List<Notification> findByUsernameAndReadFalseOrderByCreatedAtDesc(String username);

    long countByUsernameAndReadFalse(String username);

    void deleteByUsernameAndReadTrue(String username);

    void deleteByTaskId(String taskId);

    List<Notification> findByCreatedAtBefore(LocalDateTime date);
}