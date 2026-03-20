package org.example.filesapi.controller;

import org.example.filesapi.service.TaskService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;

@RestController
@RequestMapping("/apiFiles")
public class FileDiscoveryController {
private final TaskService taskService;

    public FileDiscoveryController(TaskService taskService) {
        this.taskService = taskService;
    }
    @PostMapping("/find-files")
    public Long getIdForTask() {
        taskService.
    }
}
