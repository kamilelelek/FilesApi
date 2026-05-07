package org.example.filesapi.model;

import java.util.List;

public record TaskDTO(List<String> filePaths, int numberOfResults) {
    public static TaskDTO from(Task task) {
        return new TaskDTO(task.getFilePaths().stream().map(TaskResult::getFilePath).toList(), task.getFilePaths().size());
    }
}
