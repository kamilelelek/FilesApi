package org.example.filesapi.controller;

import org.example.filesapi.model.CreateTaskCommand;
import org.example.filesapi.model.Task;
import org.example.filesapi.repository.TaskRepository;
import org.example.filesapi.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/apiFiles")
public class FileDiscoveryController {
private final TaskService taskService;
private final TaskRepository taskRepository;


    public FileDiscoveryController(TaskService taskService, TaskRepository taskRepository) {
        this.taskService = taskService;
        this.taskRepository = taskRepository;
    }
    @PostMapping("/find-files")
    public Long getIdForTask(@RequestBody CreateTaskCommand command) {
        Long jobId = taskService.runTask(command);
        return ResponseEntity.ok(jobId).getBody();
    }
    @GetMapping("/find-files/status/{jobId}")
    public ResponseEntity<Object> getTaskStatus(@PathVariable("jobId")Long  jobId) {
        Task task = taskRepository.getById(jobId);
        if (task == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(task.getStatus());
    }
    @GetMapping("/find-files/result/{jobId}")
    public ResponseEntity<Object> getTaskResult(@PathVariable("jobId")Long  jobId) {
        Task task = taskRepository.getById(jobId);
        if (task == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(task.getResult());
    }
}
