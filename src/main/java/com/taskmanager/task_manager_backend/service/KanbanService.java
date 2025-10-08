package com.taskmanager.task_manager_backend.service;

import com.taskmanager.task_manager_backend.dto.*;
import com.taskmanager.task_manager_backend.model.*;
import com.taskmanager.task_manager_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KanbanService {

    @Autowired
    private KanbanBoardRepository boardRepository;

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Create default board for new users
     */
    public KanbanBoard createDefaultBoard(String username) {
        KanbanBoard board = new KanbanBoard();
        board.setUsername(username);
        board.setName("My Task Board");

        // Create default columns matching TaskStatus
        List<KanbanColumn> columns = new ArrayList<>();
        columns.add(new KanbanColumn("todo", "To Do", 0));
        columns.add(new KanbanColumn("inprogress", "In Progress", 1));
        columns.add(new KanbanColumn("review", "Review", 2));
        columns.add(new KanbanColumn("done", "Done", 3));

        board.setColumns(columns);

        // Default settings
        BoardSettings settings = new BoardSettings();
        board.setSettings(settings);

        return boardRepository.save(board);
    }

    /**
     * Get board with tasks
     */
    public KanbanBoardDTO getBoardWithTasks(String username, String boardId) {
        KanbanBoard board = boardRepository.findByUsernameAndId(username, boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        List<Task> tasks = taskRepository.findByUsernameAndBoardIdOrderByPositionInColumnAsc(username, boardId);

        return convertToBoardDTO(board, tasks);
    }

    /**
     * Get all boards for user
     */
    public List<KanbanBoard> getUserBoards(String username) {
        return boardRepository.findByUsername(username);
    }

    /**
     * Add new column to board
     */
    public KanbanBoard addColumn(String username, String boardId, KanbanColumn column) {
        KanbanBoard board = boardRepository.findByUsernameAndId(username, boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        column.setId(UUID.randomUUID().toString());
        column.setOrder(board.getColumns().size());

        if (column.getColor() == null || column.getColor().isEmpty()) {
            column.setColor("#f3f4f6");
        }

        board.getColumns().add(column);
        board.setUpdatedAt(LocalDateTime.now());

        return boardRepository.save(board);
    }

    /**
     * Update existing column
     */
    public KanbanBoard updateColumn(String username, String boardId, String columnId, KanbanColumn updatedColumn) {
        KanbanBoard board = boardRepository.findByUsernameAndId(username, boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        board.getColumns().stream()
                .filter(col -> col.getId().equals(columnId))
                .findFirst()
                .ifPresent(col -> {
                    if (updatedColumn.getName() != null) {
                        col.setName(updatedColumn.getName());
                    }
                    if (updatedColumn.getWipLimit() != null) {
                        col.setWipLimit(updatedColumn.getWipLimit());
                    }
                    if (updatedColumn.getColor() != null) {
                        col.setColor(updatedColumn.getColor());
                    }
                });

        board.setUpdatedAt(LocalDateTime.now());
        return boardRepository.save(board);
    }

    /**
     * Delete column and move tasks to first column
     */
    @Transactional
    public KanbanBoard deleteColumn(String username, String boardId, String columnId) {
        KanbanBoard board = boardRepository.findByUsernameAndId(username, boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        if (board.getColumns().size() <= 1) {
            throw new RuntimeException("Cannot delete the last column");
        }

        List<Task> tasksInColumn = taskRepository.findByUsernameAndColumnIdOrderByPositionInColumnAsc(username, columnId);
        String firstColumnId = board.getColumns().get(0).getId();

        int position = 0;
        for (Task task : tasksInColumn) {
            task.setColumnId(firstColumnId);
            task.setPositionInColumn(position++);
            task.setUpdatedAt(LocalDateTime.now());
            taskRepository.save(task);
        }

        board.getColumns().removeIf(col -> col.getId().equals(columnId));

        for (int i = 0; i < board.getColumns().size(); i++) {
            board.getColumns().get(i).setOrder(i);
        }

        board.setUpdatedAt(LocalDateTime.now());
        return boardRepository.save(board);
    }

    /**
     * Move task between columns
     */
    @Transactional
    public Task moveTask(String username, MoveTaskRequest request) {
        Task task = taskRepository.findByIdAndUsername(request.getTaskId(), username)
                .orElseThrow(() -> new RuntimeException("Task not found or access denied"));

        task.setColumnId(request.getToColumnId());
        task.setPositionInColumn(request.getNewPosition() != null ? request.getNewPosition() : 0);

        updateTaskStatusFromColumn(task, request.getToColumnId());

        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    /**
     * Get board statistics
     */
    public Map<String, Object> getBoardStatistics(String username, String boardId) {
        List<Task> tasks = taskRepository.findByUsernameAndBoardIdOrderByPositionInColumnAsc(username, boardId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", tasks.size());
        stats.put("highPriority", tasks.stream().filter(t -> t.getPriority() == TaskPriority.HIGH).count());
        stats.put("mediumPriority", tasks.stream().filter(t -> t.getPriority() == TaskPriority.MEDIUM).count());
        stats.put("lowPriority", tasks.stream().filter(t -> t.getPriority() == TaskPriority.LOW).count());
        stats.put("overdueTasks", tasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDateTime.now())
                        // CORRECT:
                        && t.getStatus() != TaskStatus.COMPLETED)
                .count());

        return stats;
    }

    /**
     * Update board settings
     */
    public KanbanBoard updateBoardSettings(String username, String boardId, BoardSettings settings) {
        KanbanBoard board = boardRepository.findByUsernameAndId(username, boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        board.setSettings(settings);
        board.setUpdatedAt(LocalDateTime.now());
        return boardRepository.save(board);
    }

    /**
     * Reorder columns
     */
    public KanbanBoard reorderColumns(String username, String boardId, List<String> columnOrder) {
        KanbanBoard board = boardRepository.findByUsernameAndId(username, boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        Map<String, KanbanColumn> columnMap = board.getColumns().stream()
                .collect(Collectors.toMap(KanbanColumn::getId, col -> col));

        List<KanbanColumn> reorderedColumns = new ArrayList<>();
        for (int i = 0; i < columnOrder.size(); i++) {
            String columnId = columnOrder.get(i);
            KanbanColumn column = columnMap.get(columnId);
            if (column != null) {
                column.setOrder(i);
                reorderedColumns.add(column);
            }
        }

        board.setColumns(reorderedColumns);
        board.setUpdatedAt(LocalDateTime.now());
        return boardRepository.save(board);
    }

    // ====================================
    // PRIVATE HELPER METHODS
    // ====================================

    private void updateTaskStatusFromColumn(Task task, String columnId) {
        switch (columnId) {
            case "todo":
                task.setStatus(TaskStatus.TODO);
                break;
            case "inprogress":
                task.setStatus(TaskStatus.IN_PROGRESS);
                break;
            case "done":
                task.setStatus(TaskStatus.COMPLETED);
                break;
            default:
                break;
        }
    }

    private KanbanBoardDTO convertToBoardDTO(KanbanBoard board, List<Task> tasks) {
        KanbanBoardDTO dto = new KanbanBoardDTO();
        dto.setId(board.getId());
        dto.setName(board.getName());

        Map<String, List<Task>> tasksByColumn = tasks.stream()
                .collect(Collectors.groupingBy(
                        task -> task.getColumnId() != null ? task.getColumnId() : "todo"
                ));

        List<KanbanColumnDTO> columnDTOs = board.getColumns().stream()
                .sorted(Comparator.comparingInt(KanbanColumn::getOrder))
                .map(col -> {
                    KanbanColumnDTO colDTO = new KanbanColumnDTO();
                    colDTO.setId(col.getId());
                    colDTO.setName(col.getName());
                    colDTO.setOrder(col.getOrder());
                    colDTO.setWipLimit(col.getWipLimit());
                    colDTO.setColor(col.getColor());

                    List<Task> columnTasks = tasksByColumn.getOrDefault(col.getId(), new ArrayList<>());
                    columnTasks.sort(Comparator.comparing(
                            task -> task.getPositionInColumn() != null ? task.getPositionInColumn() : 0
                    ));

                    colDTO.setTasks(columnTasks.stream()
                            .map(this::convertToTaskDTO)
                            .collect(Collectors.toList()));

                    return colDTO;
                })
                .collect(Collectors.toList());

        dto.setColumns(columnDTOs);

        if (board.getSettings() != null) {
            BoardSettingsDTO settingsDTO = new BoardSettingsDTO();
            settingsDTO.setShowCompletedTasks(board.getSettings().isShowCompletedTasks());
            settingsDTO.setDefaultView(board.getSettings().getDefaultView());
            settingsDTO.setEnableWipLimits(board.getSettings().isEnableWipLimits());
            dto.setSettings(settingsDTO);
        }

        return dto;
    }

    private TaskDTO convertToTaskDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setPriority(task.getPriority().name());
        dto.setStatus(task.getStatus().name());

        if (task.getDueDate() != null) {
            dto.setDueDate(task.getDueDate().toLocalDate());
        }

        dto.setTags(task.getTags());
        dto.setColumnId(task.getColumnId());
        dto.setCategory(task.getCategory());

        return dto;
    }
}