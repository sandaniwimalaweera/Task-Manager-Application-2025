package com.taskmanager.task_manager_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "tasks")
public class Task {

    @Id
    private String id;

    @NotBlank(message = "Title cannot be empty")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Status is required")
    private TaskStatus status;

    @NotNull(message = "Priority is required")
    private TaskPriority priority;

    private LocalDateTime dueDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String username;

    private String category;

    private List<String> tags;

    // ====================================
    // KANBAN BOARD FIELDS (NEW)
    // ====================================

    /**
     * The ID of the kanban board this task belongs to
     * Null if task is not on any board
     */
    private String boardId;

    /**
     * The ID of the column this task is in on the kanban board
     * Null if task is not on any board
     */
    private String columnId;

    /**
     * The position of this task within its column
     * Used for ordering tasks in the UI
     */
    private Integer positionInColumn;

    // Default constructor
    public Task() {
    }

    // Constructor with required fields
    public Task(String title, TaskStatus status, TaskPriority priority, String username) {
        this.title = title;
        this.status = status;
        this.priority = priority;
        this.username = username;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters (Original)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    // ====================================
    // KANBAN GETTERS AND SETTERS (NEW)
    // ====================================

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public String getColumnId() {
        return columnId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public Integer getPositionInColumn() {
        return positionInColumn;
    }

    public void setPositionInColumn(Integer positionInColumn) {
        this.positionInColumn = positionInColumn;
    }

    // ====================================
    // UTILITY METHODS
    // ====================================

    /**
     * Check if this task is currently on a kanban board
     */
    public boolean isOnBoard() {
        return boardId != null && columnId != null;
    }

    /**
     * Remove task from kanban board
     */
    public void removeFromBoard() {
        this.boardId = null;
        this.columnId = null;
        this.positionInColumn = null;
    }

    /**
     * Update the updatedAt timestamp
     */
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", username='" + username + '\'' +
                ", boardId='" + boardId + '\'' +
                ", columnId='" + columnId + '\'' +
                ", positionInColumn=" + positionInColumn +
                '}';
    }
}