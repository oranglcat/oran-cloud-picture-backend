package com.oran.oranpicturebackend.common;

public enum TaskStatus {
    RUNNING("running"),
    SUCCESS("succeed"),
    FAILED("failed"),
    WAIT("wait");

    private final String status;

    TaskStatus(String status) {
        this.status = status;
    }
    public String getValue() {
        return status;
    }

}
