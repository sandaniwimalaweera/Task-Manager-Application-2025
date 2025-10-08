package com.taskmanager.task_manager_backend.dto;

public class BoardSettingsDTO {

    private boolean showCompletedTasks;
    private String defaultView;
    private boolean enableWipLimits;

    public BoardSettingsDTO() {}

    // Getters and Setters
    public boolean isShowCompletedTasks() {
        return showCompletedTasks;
    }

    public void setShowCompletedTasks(boolean showCompletedTasks) {
        this.showCompletedTasks = showCompletedTasks;
    }

    public String getDefaultView() {
        return defaultView;
    }

    public void setDefaultView(String defaultView) {
        this.defaultView = defaultView;
    }

    public boolean isEnableWipLimits() {
        return enableWipLimits;
    }

    public void setEnableWipLimits(boolean enableWipLimits) {
        this.enableWipLimits = enableWipLimits;
    }
}
