
package com.taskmanager.task_manager_backend.model;

import java.util.ArrayList;
import java.util.List;

public class KanbanColumn {

    private String id;
    private String name;
    private int order;
    private Integer wipLimit;
    private String color;
    private List<String> taskIds = new ArrayList<>();

    public KanbanColumn() {}

    public KanbanColumn(String id, String name, int order) {
        this.id = id;
        this.name = name;
        this.order = order;
        this.taskIds = new ArrayList<>();
        this.color = "#f3f4f6"; // Default gray color
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Integer getWipLimit() {
        return wipLimit;
    }

    public void setWipLimit(Integer wipLimit) {
        this.wipLimit = wipLimit;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<String> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<String> taskIds) {
        this.taskIds = taskIds;
    }
}