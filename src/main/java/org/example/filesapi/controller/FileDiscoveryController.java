package org.example.filesapi.controller;

import org.example.filesapi.model.CreateTaskCommand;
import org.example.filesapi.model.Task;
import org.example.filesapi.model.TaskStatus;
import org.example.filesapi.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/apiFiles")
public class FileDiscoveryController {
private final TaskService taskService;
private static final Logger log = LoggerFactory.getLogger(FileDiscoveryController.class);

// https://www.baeldung.com/slf4j-with-log4j2-logback


    public FileDiscoveryController(TaskService taskService) {
        this.taskService = taskService;
    }
    @PostMapping("/find-files")
    public ResponseEntity<UUID> createTask(@RequestBody CreateTaskCommand command) {
        UUID jobId = taskService.createTask(command);
        return ResponseEntity.ok(jobId);
    }
    @GetMapping("/find-files/status/{jobId}")
    public ResponseEntity<Object> getTaskStatus(@PathVariable("jobId") UUID jobId) {
        log.info("Staring procesing job");
        if (taskService.getTaskStatus(jobId) == TaskStatus.Running) {
            return ResponseEntity.status(400).body("Job is still running");
        }
        return ResponseEntity.ok(taskService.getTaskStatus(jobId));
    }
    @GetMapping("/find-files/result/{jobId}")
    public ResponseEntity<Object> getTaskResult(@PathVariable("jobId") UUID jobId) {
        if(jobId.) {}
        if (taskService.getTask(jobId) == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(taskService.getTask(jobId).getFilePaths());
    }
}
