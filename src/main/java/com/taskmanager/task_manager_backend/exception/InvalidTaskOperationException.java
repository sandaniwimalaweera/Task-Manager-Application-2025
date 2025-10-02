package com.taskmanager.task_manager_backend.exception;

public class InvalidTaskOperationException extends RuntimeException {
    public InvalidTaskOperationException(String message) {
        super(message);
    }

    public InvalidTaskOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}