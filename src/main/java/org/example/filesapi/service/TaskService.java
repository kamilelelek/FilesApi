package org.example.filesapi.service;

import org.example.filesapi.model.CreateTaskCommand;
import org.example.filesapi.model.Task;
import org.example.filesapi.model.TaskResult;
import org.example.filesapi.model.TaskStatus;
import org.example.filesapi.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final ExecutorService executorService;

    public TaskService(TaskRepository taskRepository, ExecutorService executorService) {
        this.taskRepository = taskRepository;
        this.executorService = executorService;
    }

    public UUID runTask(CreateTaskCommand command) {
        Task task = new Task();
        task.setStatus(TaskStatus.Running);
        Task savedTask = taskRepository.save(task);
        executorService.submit(() -> {
            try {
                List<File> files = searchFilesWithExtension(command);
                List<TaskResult> results = files.stream()
                        .map(file -> {
                            TaskResult tr = new TaskResult();
                            tr.setFilePath(file.getAbsolutePath());
                            tr.setTask(savedTask);
                            return tr;
                        })
                        .collect(Collectors.toList());
                savedTask.setFilePaths(results);
                savedTask.setStatus(TaskStatus.Completed);
            } catch (InterruptedException e) {
                savedTask.setStatus(TaskStatus.Failed);
            } catch (Exception e) {
                savedTask.setStatus(TaskStatus.Failed);
                log.error("Task {} failed: {}", savedTask.getTaskId(), e.getMessage());
            } finally {
                taskRepository.save(savedTask);
            }
        });
        return savedTask.getTaskId();
    }

    public List<File> searchFilesWithExtension(CreateTaskCommand command) throws InterruptedException {
        log.debug("Searching for files with the extension {}", command.getExtension());

        Path path = Paths.get(command.getSource().toLowerCase());
        File root = path.toFile();
        if (!root.exists()) {
            throw new IllegalArgumentException("Directory does not exist: " + command.getSource());
        }
        if (!root.isDirectory()) {
            throw new IllegalArgumentException("Path is not a directory: " + command.getSource());
        }
        List<File> files = new ArrayList<>(showFiles(root, command));
        files.forEach(p -> log.info("File {} has been found", p));

        if (files.isEmpty()) {
            log.info("No files found with extension {} in directory {}", command.getExtension(), command.getSource());
        }
        Thread.sleep(1000);
        return new ArrayList<>(files);
    }

    private Collection<? extends File> showFiles(File folder, CreateTaskCommand command) throws InterruptedException {
        List<File> result = new ArrayList<>();
        File[] files = folder.listFiles();

        if (files == null) return result;

        for (File file : files) {
            if (file.isDirectory()) {
                result.addAll(showFiles(file, command));
            } else if (file.getName().toLowerCase().endsWith(command.getExtension().toLowerCase())) {
                result.add(file);
            }
        }
        return result;
    }

    public UUID createTask(CreateTaskCommand command) {
        UUID jobId = runTask(command);
        log.info("Job ID: " + jobId);
        return jobId;
    }

    public TaskStatus getTaskStatus(UUID jobId) {
        Task task = taskRepository.findById(jobId)
                .orElseThrow(() -> new NoSuchElementException("Task not found: " + jobId));
        return task.getStatus();
    }

    public Task getTask(UUID jobId) {
        return taskRepository.findById(jobId)
                .orElseThrow(() -> new NoSuchElementException("Task not found: " + jobId));
    }
}
