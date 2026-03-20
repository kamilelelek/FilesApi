package org.example.filesapi.service;

import org.example.filesapi.model.CreateTaskCommand;
import org.example.filesapi.model.ExecutorConfig;
import org.example.filesapi.model.Task;
import org.example.filesapi.model.TaskStatus;
import org.example.filesapi.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ExecutorService executorService;

    public TaskService(TaskRepository taskRepository, ExecutorService executorService) {
        this.taskRepository = taskRepository;
        this.executorService = executorService;
    }


    public Long runTask(CreateTaskCommand command) {
        Long taskId = taskRepository.generateId();
        Task task=new Task(taskId);
        task.setTaskId(taskId);
        task.setStatus(TaskStatus.Running);
        executorService.submit(() -> {
            try {
                List<File> files=searchFilesWithExtension(command);
                task.setResult(new ArrayList<>(files));
                task.setStatus(TaskStatus.Completed);
            } catch (InterruptedException e) {
                task.setStatus(TaskStatus.Failed);
            }
        });
                return task.getTaskId();
    }


    public List<File> searchFilesWithExtension(CreateTaskCommand command) throws InterruptedException {
        File direction = new File(command.getSource());
        File[] files = direction.listFiles((dir, name) ->
                name.toLowerCase().endsWith(command.getExtension()));

        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("No such file or directory");
        }
        List<File> result = new ArrayList<>();
        for (File file : files) {
            result.add(file);
            Thread.sleep(1000);
        }
        return result;
    }
}
