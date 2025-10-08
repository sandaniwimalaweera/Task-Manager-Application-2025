
package com.taskmanager.task_manager_backend.dto;

public class MoveTaskRequest {

    private String taskId;
    private String fromColumnId;
    private String toColumnId;
    private Integer newPosition;

    public MoveTaskRequest() {}

    // Getters and Setters
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getFromColumnId() {
        return fromColumnId;
    }

    public void setFromColumnId(String fromColumnId) {
        this.fromColumnId = fromColumnId;
    }

    public String getToColumnId() {
        return toColumnId;
    }

    public void setToColumnId(String toColumnId) {
        this.toColumnId = toColumnId;
    }

    public Integer getNewPosition() {
        return newPosition;
    }

    public void setNewPosition(Integer newPosition) {
        this.newPosition = newPosition;
    }
}