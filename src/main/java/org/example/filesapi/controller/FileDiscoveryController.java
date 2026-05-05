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
            return ResponseEntity.status(204).body("Job is still running"); //task created, no content
        }
        if (taskService.getTaskStatus(jobId)== TaskStatus.Completed){
            return ResponseEntity.status(200).body("Job is completed, check your result");
        }
        return ResponseEntity.ok(taskService.getTaskStatus(jobId));
    }

    // przerób tak aby zwracać nie całego taksa ale DTO TYLKO z fileList i numeberOFFiles, UUID nas nie interere
    // DTO -> powinno działać dla obu scenariuszy zarówno dla erroru jak i rezultatu poprawnego

    @GetMapping("/find-files/result/{jobId}")
    public ResponseEntity<?> getTaskResult(@PathVariable("jobId") UUID jobId) {
        if (taskService.getTaskStatus(jobId) == TaskStatus.Failed) {
            return ResponseEntity.status(404).body("Task is failed");
        }
        Task task = taskService.getTask(jobId);
        return ResponseEntity.ok(task);
    }
}
