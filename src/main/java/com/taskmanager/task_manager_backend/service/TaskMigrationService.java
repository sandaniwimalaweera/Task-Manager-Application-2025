package com.taskmanager.task_manager_backend.service;

import com.taskmanager.task_manager_backend.model.KanbanBoard;
import com.taskmanager.task_manager_backend.model.Task;
import com.taskmanager.task_manager_backend.model.TaskStatus;
import com.taskmanager.task_manager_backend.repository.KanbanBoardRepository;
import com.taskmanager.task_manager_backend.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskMigrationService {

    private static final Logger logger = LoggerFactory.getLogger(TaskMigrationService.class);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private KanbanBoardRepository boardRepository;

    @Autowired
    private KanbanService kanbanService;

    /**
     * Migrate all tasks without a board to the user's default board
     */
    @Transactional
    public int migrateUserTasksToDefaultBoard(String username) {
        logger.info("Starting task migration for user: {}", username);

        List<Task> unboardedTasks = taskRepository.findByUsernameAndBoardIdIsNull(username);

        if (unboardedTasks.isEmpty()) {
            logger.info("No tasks to migrate for user: {}", username);
            return 0;
        }

        KanbanBoard board = getOrCreateDefaultBoard(username);

        int migratedCount = 0;
        for (Task task : unboardedTasks) {
            String columnId = mapStatusToColumnId(task.getStatus());

            task.setBoardId(board.getId());
            task.setColumnId(columnId);
            task.setPositionInColumn(migratedCount);
            task.setUpdatedAt(java.time.LocalDateTime.now());

            taskRepository.save(task);
            migratedCount++;

            logger.debug("Migrated task '{}' to column '{}'", task.getTitle(), columnId);
        }

        logger.info("Successfully migrated {} tasks for user: {}", migratedCount, username);
        return migratedCount;
    }

    /**
     * Migrate a specific task to a board
     */
    @Transactional
    public Task migrateTaskToBoard(String taskId, String username, String boardId) {
        Task task = taskRepository.findByIdAndUsername(taskId, username)
                .orElseThrow(() -> new RuntimeException("Task not found or access denied"));

        if (task.getBoardId() != null) {
            throw new RuntimeException("Task is already on a board");
        }

        KanbanBoard board;
        if (boardId != null) {
            board = boardRepository.findByUsernameAndId(username, boardId)
                    .orElseThrow(() -> new RuntimeException("Board not found"));
        } else {
            board = getOrCreateDefaultBoard(username);
        }

        String columnId = mapStatusToColumnId(task.getStatus());

        task.setBoardId(board.getId());
        task.setColumnId(columnId);
        task.setPositionInColumn(0);
        task.setUpdatedAt(java.time.LocalDateTime.now());

        return taskRepository.save(task);
    }

    /**
     * Remove task from board
     */
    @Transactional
    public Task removeTaskFromBoard(String taskId, String username) {
        Task task = taskRepository.findByIdAndUsername(taskId, username)
                .orElseThrow(() -> new RuntimeException("Task not found or access denied"));

        if (task.getBoardId() == null) {
            throw new RuntimeException("Task is not on any board");
        }

        task.setBoardId(null);
        task.setColumnId(null);
        task.setPositionInColumn(null);
        task.setUpdatedAt(java.time.LocalDateTime.now());

        return taskRepository.save(task);
    }

    /**
     * Get or create default board
     */
    private KanbanBoard getOrCreateDefaultBoard(String username) {
        List<KanbanBoard> boards = boardRepository.findByUsername(username);

        if (!boards.isEmpty()) {
            return boards.get(0);
        }

        logger.info("Creating default board for user: {}", username);
        return kanbanService.createDefaultBoard(username);
    }

    /**
     * Map TaskStatus to column IDs
     */
    private String mapStatusToColumnId(TaskStatus status) {
        switch (status) {
            case TODO:
                return "todo";
            case IN_PROGRESS:
                return "inprogress";
            case COMPLETED:
                return "done";
            default:
                return "todo";
        }
    }

    /**
     * Check if user has unboarded tasks
     */
    public boolean hasUnboardedTasks(String username) {
        return !taskRepository.findByUsernameAndBoardIdIsNull(username).isEmpty();
    }

    /**
     * Get count of unboarded tasks
     */
    public long getUnboardedTaskCount(String username) {
        return taskRepository.findByUsernameAndBoardIdIsNull(username).size();
    }
}