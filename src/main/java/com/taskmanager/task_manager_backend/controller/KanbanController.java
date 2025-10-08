package com.taskmanager.task_manager_backend.controller;

import com.taskmanager.task_manager_backend.dto.*;
import com.taskmanager.task_manager_backend.model.*;
import com.taskmanager.task_manager_backend.service.KanbanService;
import com.taskmanager.task_manager_backend.service.TaskMigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kanban")
@CrossOrigin(origins = "http://localhost:4200")
public class KanbanController {

    @Autowired
    private KanbanService kanbanService;

    @Autowired
    private TaskMigrationService migrationService;

    // ====================================
    // BOARD MANAGEMENT
    // ====================================

    /**
     * Get all boards for the authenticated user
     * GET /api/kanban/boards
     */
    @GetMapping("/boards")
    public ResponseEntity<List<KanbanBoard>> getUserBoards(Authentication auth) {
        String username = auth.getName();
        List<KanbanBoard> boards = kanbanService.getUserBoards(username);
        return ResponseEntity.ok(boards);
    }

    /**
     * Get a specific board with all its tasks
     * GET /api/kanban/boards/{boardId}
     */
    @GetMapping("/boards/{boardId}")
    public ResponseEntity<KanbanBoardDTO> getBoard(
            @PathVariable String boardId,
            Authentication auth) {
        try {
            String username = auth.getName();
            KanbanBoardDTO board = kanbanService.getBoardWithTasks(username, boardId);
            return ResponseEntity.ok(board);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Create a new default board
     * POST /api/kanban/boards
     */
    @PostMapping("/boards")
    public ResponseEntity<KanbanBoard> createBoard(Authentication auth) {
        String username = auth.getName();
        KanbanBoard board = kanbanService.createDefaultBoard(username);
        return ResponseEntity.status(HttpStatus.CREATED).body(board);
    }

    /**
     * Get board statistics
     * GET /api/kanban/boards/{boardId}/statistics
     */
    @GetMapping("/boards/{boardId}/statistics")
    public ResponseEntity<Map<String, Object>> getBoardStatistics(
            @PathVariable String boardId,
            Authentication auth) {
        String username = auth.getName();
        Map<String, Object> stats = kanbanService.getBoardStatistics(username, boardId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Update board settings
     * PUT /api/kanban/boards/{boardId}/settings
     */
    @PutMapping("/boards/{boardId}/settings")
    public ResponseEntity<KanbanBoard> updateBoardSettings(
            @PathVariable String boardId,
            @RequestBody BoardSettings settings,
            Authentication auth) {
        try {
            String username = auth.getName();
            KanbanBoard board = kanbanService.updateBoardSettings(username, boardId, settings);
            return ResponseEntity.ok(board);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // ====================================
    // COLUMN MANAGEMENT
    // ====================================

    /**
     * Add a new column to a board
     * POST /api/kanban/boards/{boardId}/columns
     */
    @PostMapping("/boards/{boardId}/columns")
    public ResponseEntity<KanbanBoard> addColumn(
            @PathVariable String boardId,
            @RequestBody KanbanColumn column,
            Authentication auth) {
        try {
            String username = auth.getName();
            KanbanBoard board = kanbanService.addColumn(username, boardId, column);
            return ResponseEntity.status(HttpStatus.CREATED).body(board);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Update an existing column
     * PUT /api/kanban/boards/{boardId}/columns/{columnId}
     */
    @PutMapping("/boards/{boardId}/columns/{columnId}")
    public ResponseEntity<KanbanBoard> updateColumn(
            @PathVariable String boardId,
            @PathVariable String columnId,
            @RequestBody KanbanColumn column,
            Authentication auth) {
        try {
            String username = auth.getName();
            KanbanBoard board = kanbanService.updateColumn(username, boardId, columnId, column);
            return ResponseEntity.ok(board);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Delete a column (tasks will be moved to first column)
     * DELETE /api/kanban/boards/{boardId}/columns/{columnId}
     */
    @DeleteMapping("/boards/{boardId}/columns/{columnId}")
    public ResponseEntity<KanbanBoard> deleteColumn(
            @PathVariable String boardId,
            @PathVariable String columnId,
            Authentication auth) {
        try {
            String username = auth.getName();
            KanbanBoard board = kanbanService.deleteColumn(username, boardId, columnId);
            return ResponseEntity.ok(board);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Reorder columns on a board
     * PUT /api/kanban/boards/{boardId}/columns/reorder
     */
    @PutMapping("/boards/{boardId}/columns/reorder")
    public ResponseEntity<KanbanBoard> reorderColumns(
            @PathVariable String boardId,
            @RequestBody List<String> columnOrder,
            Authentication auth) {
        try {
            String username = auth.getName();
            KanbanBoard board = kanbanService.reorderColumns(username, boardId, columnOrder);
            return ResponseEntity.ok(board);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // ====================================
    // TASK MANAGEMENT
    // ====================================

    /**
     * Move a task between columns
     * PUT /api/kanban/tasks/move
     */
    @PutMapping("/tasks/move")
    public ResponseEntity<Task> moveTask(
            @RequestBody MoveTaskRequest request,
            Authentication auth) {
        try {
            String username = auth.getName();
            Task task = kanbanService.moveTask(username, request);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // ====================================
    // MIGRATION ENDPOINTS
    // ====================================

    /**
     * Check if user has tasks that need migration
     * GET /api/kanban/migration/status
     */
    @GetMapping("/migration/status")
    public ResponseEntity<Map<String, Object>> getMigrationStatus(Authentication auth) {
        String username = auth.getName();
        boolean hasUnboardedTasks = migrationService.hasUnboardedTasks(username);
        long unboardedCount = migrationService.getUnboardedTaskCount(username);

        Map<String, Object> status = new HashMap<>();
        status.put("hasUnboardedTasks", hasUnboardedTasks);
        status.put("unboardedTaskCount", unboardedCount);
        status.put("needsMigration", hasUnboardedTasks);

        return ResponseEntity.ok(status);
    }

    /**
     * Migrate all user's tasks to default board
     * POST /api/kanban/migration/migrate
     */
    @PostMapping("/migration/migrate")
    public ResponseEntity<Map<String, Object>> migrateUserTasks(Authentication auth) {
        try {
            String username = auth.getName();
            int migratedCount = migrationService.migrateUserTasksToDefaultBoard(username);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("migratedTaskCount", migratedCount);
            result.put("message", "Successfully migrated " + migratedCount + " tasks");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Migration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Migrate a specific task to a board
     * POST /api/kanban/tasks/{taskId}/migrate
     */
    @PostMapping("/tasks/{taskId}/migrate")
    public ResponseEntity<Task> migrateTask(
            @PathVariable String taskId,
            @RequestParam(required = false) String boardId,
            Authentication auth) {
        try {
            String username = auth.getName();
            Task task = migrationService.migrateTaskToBoard(taskId, username, boardId);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Remove a task from board (move back to list view)
     * POST /api/kanban/tasks/{taskId}/remove-from-board
     */
    @PostMapping("/tasks/{taskId}/remove-from-board")
    public ResponseEntity<Task> removeTaskFromBoard(
            @PathVariable String taskId,
            Authentication auth) {
        try {
            String username = auth.getName();
            Task task = migrationService.removeTaskFromBoard(taskId, username);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // ====================================
    // ERROR HANDLING
    // ====================================

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}