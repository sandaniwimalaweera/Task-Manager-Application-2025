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

    // ====================================
    // EXISTING METHODS (Keep all these)
    // ====================================

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

    // ====================================
    // NEW KANBAN BOARD METHODS
    // ====================================

    /**
     * Find all tasks for a user on a specific board
     * Ordered by position in column
     */
    List<Task> findByUsernameAndBoardIdOrderByPositionInColumnAsc(String username, String boardId);

    /**
     * Find all tasks in a specific column
     * Ordered by position
     */
    List<Task> findByUsernameAndColumnIdOrderByPositionInColumnAsc(String username, String columnId);

    /**
     * Find tasks in a specific board and column
     */
    List<Task> findByUsernameAndBoardIdAndColumnId(String username, String boardId, String columnId);

    /**
     * Find all tasks NOT on any board (for backwards compatibility)
     * These are tasks created before Kanban feature or manually removed from board
     */
    List<Task> findByUsernameAndBoardIdIsNull(String username);

    /**
     * Find all tasks on any board (opposite of above)
     */
    List<Task> findByUsernameAndBoardIdIsNotNull(String username);

    /**
     * Count tasks in a specific column
     */
    long countByUsernameAndColumnId(String username, String columnId);

    /**
     * Count tasks on a specific board
     */
    long countByUsernameAndBoardId(String username, String boardId);

    /**
     * Find tasks by username and board, with status filter
     */
    List<Task> findByUsernameAndBoardIdAndStatus(String username, String boardId, TaskStatus status);

    /**
     * Find tasks by username and board, with priority filter
     */
    List<Task> findByUsernameAndBoardIdAndPriority(String username, String boardId, TaskPriority priority);

    /**
     * Find overdue tasks on a specific board
     */
    List<Task> findByUsernameAndBoardIdAndDueDateBeforeAndStatusNot(
            String username,
            String boardId,
            LocalDateTime date,
            TaskStatus status
    );

    /**
     * Find tasks by category on a specific board
     */
    List<Task> findByUsernameAndBoardIdAndCategory(String username, String boardId, String category);

    /**
     * Find tasks by tag on a specific board
     */
    List<Task> findByUsernameAndBoardIdAndTagsContaining(String username, String boardId, String tag);

    /**
     * Search tasks on a board by title or description
     */
    List<Task> findByUsernameAndBoardIdAndTitleContainingIgnoreCaseOrUsernameAndBoardIdAndDescriptionContainingIgnoreCase(
            String username1, String boardId1, String titleKeyword,
            String username2, String boardId2, String descriptionKeyword
    );
}