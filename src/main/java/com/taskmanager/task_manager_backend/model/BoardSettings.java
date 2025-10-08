
package com.taskmanager.task_manager_backend.model;

public class BoardSettings {

    private boolean showCompletedTasks = true;
    private String defaultView = "detailed";
    private boolean enableWipLimits = false;

    public BoardSettings() {}

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
