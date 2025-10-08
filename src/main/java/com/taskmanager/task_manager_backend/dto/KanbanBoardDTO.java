
package com.taskmanager.task_manager_backend.dto;

import java.util.List;

public class KanbanBoardDTO {

    private String id;
    private String name;
    private List<KanbanColumnDTO> columns;
    private BoardSettingsDTO settings;

    public KanbanBoardDTO() {}

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

    public List<KanbanColumnDTO> getColumns() {
        return columns;
    }

    public void setColumns(List<KanbanColumnDTO> columns) {
        this.columns = columns;
    }

    public BoardSettingsDTO getSettings() {
        return settings;
    }

    public void setSettings(BoardSettingsDTO settings) {
        this.settings = settings;
    }
}
