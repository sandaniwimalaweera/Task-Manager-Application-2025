package com.taskmanager.task_manager_backend.repository;

import com.taskmanager.task_manager_backend.model.Task;
import com.taskmanager.task_manager_backend.model.TaskPriority;
import com.taskmanager.task_manager_backend.model.TaskStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {

    List<Task> findByUsername(String username);

    Optional<Task> findByIdAndUsername(String id, String username);

    List<Task> findByStatusAndUsername(TaskStatus status, String username);

    List<Task> findByPriorityAndUsername(TaskPriority priority, String username);

    List<Task> findByUsernameAndTitleContainingIgnoreCaseOrUsernameAndDescriptionContainingIgnoreCase(
            String username1, String titleKeyword, String username2, String descriptionKeyword);

    long countByUsername(String username);
    long countByStatusAndUsername(TaskStatus status, String username);
    long countByPriorityAndUsername(TaskPriority priority, String username);

    long countByUsernameAndDueDateBeforeAndStatusNot(String username, LocalDateTime dateTime, TaskStatus status);

    List<Task> findByUsernameAndDueDateBefore(String username, LocalDateTime dateTime);
    List<Task> findByUsernameOrderByCreatedAtDesc(String username);
    List<Task> findByUsernameOrderByDueDateAsc(String username);
}