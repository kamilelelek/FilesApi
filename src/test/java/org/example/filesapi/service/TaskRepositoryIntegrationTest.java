package org.example.filesapi.service;

import org.example.filesapi.model.CreateTaskCommand;
import org.example.filesapi.model.Task;
import org.example.filesapi.model.TaskResult;
import org.example.filesapi.model.TaskStatus;
import org.example.filesapi.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class TaskRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskService taskService;

    @Test
    void shouldSaveAndFindTaskById() {
        // GIVEN
        Task task = new Task();
        task.setStatus(TaskStatus.Running);

        // WHEN
        Task saved = taskRepository.save(task);
        Optional<Task> found = taskRepository.findById(saved.getTaskId());

        // THEN
        assertTrue(found.isPresent());
        assertEquals(TaskStatus.Running, found.get().getStatus());
    }

    @Test
    void shouldUpdateTaskStatus() {
        // GIVEN
        Task task = new Task();
        task.setStatus(TaskStatus.Running);
        Task saved = taskRepository.save(task);

        // WHEN
        saved.setStatus(TaskStatus.Completed);
        taskRepository.save(saved);
        Optional<Task> found = taskRepository.findById(saved.getTaskId());

        // THEN
        assertTrue(found.isPresent());
        assertEquals(TaskStatus.Completed, found.get().getStatus());
    }

    @Test
    void shouldSaveResultOfTask() {
        // GIVEN
        Task task = new Task();
        task.setStatus(TaskStatus.Completed);
        Task saved = taskRepository.save(task);

        TaskResult result = new TaskResult();
        result.setFilePath("/tmp/test.txt");
        result.setTask(saved);
        saved.setFilePaths(List.of(result));
        taskRepository.save(saved);

        // WHEN
        Optional<Task> found = taskRepository.findById(saved.getTaskId());

        // THEN
        assertTrue(found.isPresent());
        assertFalse(found.get().getFilePaths().isEmpty());
        assertEquals("/tmp/test.txt", found.get().getFilePaths().get(0).getFilePath());
    }

    @Test
    void existsByIdShouldReturnCorrectly() {
        // GIVEN
        Task task = new Task();
        task.setStatus(TaskStatus.Running);
        Task saved = taskRepository.save(task);

        // THEN
        assertTrue(taskRepository.existsById(saved.getTaskId()));
        assertFalse(taskRepository.existsById(UUID.randomUUID()));
    }

    @Test
    void shouldRunTaskAndSaveResultsToDatabase(@TempDir Path tempDir) throws IOException {
        // GIVEN
        Files.createFile(tempDir.resolve("file1.txt"));
        Files.createFile(tempDir.resolve("file2.txt"));
        CreateTaskCommand command = new CreateTaskCommand(".txt", tempDir.toString());

        // WHEN
        UUID jobId = taskService.runTask(command);

        // THEN
        await().atMost(5, TimeUnit.SECONDS).until(() ->
                taskRepository.findById(jobId)
                        .map(t -> t.getStatus() == TaskStatus.Completed)
                        .orElse(false)
        );

        Task completed = taskRepository.findById(jobId).orElseThrow();
        assertEquals(TaskStatus.Completed, completed.getStatus());
        assertEquals(2, completed.getFilePaths().size());
    }
}
