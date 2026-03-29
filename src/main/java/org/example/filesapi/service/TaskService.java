package org.example.filesapi.service;

import org.example.filesapi.model.CreateTaskCommand;
import org.example.filesapi.model.Task;
import org.example.filesapi.model.TaskStatus;
import org.example.filesapi.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;

@Service
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final ExecutorService executorService;

    public TaskService(TaskRepository taskRepository, ExecutorService executorService) {
        this.taskRepository = taskRepository;
        this.executorService = executorService;
    }


    public Long runTask(CreateTaskCommand command) {
        Long taskId = taskRepository.generateId();
        Task task=new Task(taskId);
        task.setStatus(TaskStatus.Running);
        taskRepository.addTask(task);
        executorService.submit(() -> {
            try {
                List<File> files = searchFilesWithExtension(command);
                task.setResult(new ArrayList<>(files));
                task.setStatus(TaskStatus.Completed);
            } catch (InterruptedException e) {
                task.setStatus(TaskStatus.Failed);
            } catch (Exception e) {
                task.setStatus(TaskStatus.Failed);
                log.error("Task {} failed: {}", task.getTaskId(), e.getMessage());
            }
        });
                return task.getTaskId();
    }


    public List<File> searchFilesWithExtension(CreateTaskCommand command) throws InterruptedException {
        log.debug("Searching for files with the extension {}", command.getExtension());

        // Lista folder
        // Try-with-resources !!!!!!!!!!!!!
        // Rekurencja
        Path path = Paths.get(command.getSource().toLowerCase());
        File root = path.toFile();
        if (!root.exists()) {
            throw new IllegalArgumentException("Directory does not exist: " + command.getSource());
        }
        if (!root.isDirectory()) {
            throw new IllegalArgumentException("Path is not a directory: " + command.getSource());
        }
        File[] filesFromPath = root.listFiles();
        assert filesFromPath != null;
        List<File> files = new ArrayList<>(showFiles(root, command));
        files.forEach(p -> log.info("File {} has been found", p));

        if (files.isEmpty()) {
            log.info("No files found with extension {} in directory {}", command.getExtension(), command.getSource());        }
        Thread.sleep(1000);
        return new ArrayList<>(files);
    }
    private Collection<? extends File> showFiles(File folder, CreateTaskCommand command) throws InterruptedException {
        List<File> result = new ArrayList<>();
        File[] files= folder.listFiles();

        if (files == null) return result;

        for (File file : files) {
            if (file.isDirectory()) {
                result.addAll(showFiles(file,command)); // wchodzi głębiej
            } else if (file.getName().toLowerCase().endsWith(command.getExtension().toLowerCase())) {
                result.add(file); // znalazł plik z właściwym rozszerzeniem
            }
        }
        return result;
    }
    public long createTask(CreateTaskCommand command) {
        Long jobId = runTask(command);
        log.info("Job ID: " + jobId);
        return  jobId;
    }
    public TaskStatus getTaskStatus(Long jobId) {
        Task task = taskRepository.getById(jobId);
        if (task == null) {
            throw new NoSuchElementException("Task not found: " + jobId);
        }
        return task.getStatus();
    }
    public Task getTask(Long jobId) {
        Task task = taskRepository.getById(jobId);
        if (task == null) {
            throw new NoSuchElementException("Task not found: " + jobId);
        }
        return task;
    }

}
