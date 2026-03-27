package org.example.filesapi.model;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

@Setter
@Getter
public class Task {
    private final long taskId;
    private volatile TaskStatus status;
    private volatile ArrayList<File> result;
    public Task(Long taskId) {
        this.taskId = taskId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(taskId, task.taskId) && status == task.status && Objects.equals(result, task.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }
}
