package org.example.filesapi.service;

import org.example.filesapi.model.CreateTaskCommand;
import org.example.filesapi.model.Task;
import org.example.filesapi.model.TaskStatus;
import org.example.filesapi.repository.TaskRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;

    private TaskService taskService;
    private CreateTaskCommand command;

    private final Map<UUID, Task> db = new ConcurrentHashMap<>();

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, Executors.newFixedThreadPool(4));
        command = new CreateTaskCommand();
        db.clear();

        lenient().when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            if (t.getTaskId() == null) {
                t.setTaskId(UUID.randomUUID());
            }
            db.put(t.getTaskId(), t);
            return t;
        });
        lenient().when(taskRepository.findById(any(UUID.class))).thenAnswer(inv ->
                Optional.ofNullable(db.get(inv.<UUID>getArgument(0))));
    }

    @Test
    void shouldFindFilesWithSpecificExtension(@TempDir Path tempDir) throws IOException, InterruptedException {
        // GIVEN: happy path
        Files.createFile(tempDir.resolve("test1.txt"));
        Files.createFile(tempDir.resolve("test2.txt"));
        Files.createFile(tempDir.resolve("test3.txt"));
        // WHEN
        command.setSource(tempDir.toString());
        command.setExtension(".txt");
        List<File> result = taskService.searchFilesWithExtension(command);
        // THEN
        Assertions.assertEquals(3, result.size());
    }

    @Test
    void emptyFolder(@TempDir Path tempDir) throws InterruptedException {
        // GIVEN
        assertTrue(Files.exists(tempDir), "JUnit TempDir should exist");
        // WHEN
        command.setSource(tempDir.toAbsolutePath().toString());
        command.setExtension(".txt");
        List<File> result = taskService.searchFilesWithExtension(command);
        // THEN
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    void wrongPath() throws InterruptedException {
        // GIVEN
        command.setSource("text.txt");
        command.setExtension("text.txt");
        // THEN
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            taskService.searchFilesWithExtension(command);
        });
        Assertions.assertTrue(exception.getMessage().contains("Directory does not exist"));
    }

    @Test
    void shouldFindFiles(@TempDir Path tempDir) throws IOException, InterruptedException {
        Files.createFile(tempDir.resolve("test1.TXT"));
        Files.createFile(tempDir.resolve("test2.TxT"));
        Files.createFile(tempDir.resolve("test3.Txt"));
        // WHEN
        command.setSource(tempDir.toString());
        command.setExtension(".Txt");
        List<File> result = taskService.searchFilesWithExtension(command);
        // THEN
        Assertions.assertEquals(3, result.size());
    }

    @Test
    void shouldRunTaskAndCompleteSuccessfully(@TempDir Path tempDir) throws IOException, InterruptedException {
        // GIVEN
        Files.createFile(tempDir.resolve("data.txt"));
        TaskService service = new TaskService(taskRepository, Executors.newFixedThreadPool(1));
        command.setSource(tempDir.toString());
        command.setExtension(".txt");
        // WHEN
        UUID taskId = service.runTask(command);
        // THEN
        Thread.sleep(1500);

        Task saved = db.get(taskId);
        Assertions.assertNotNull(saved);
        Assertions.assertEquals(TaskStatus.Completed, saved.getStatus());
        Assertions.assertFalse(saved.getFilePaths().isEmpty());
    }

    @Test
    void shouldCreateTaskAndReturnStatus(@TempDir Path tempDir) throws InterruptedException {
        // GIVEN
        command.setSource(tempDir.toString());
        command.setExtension(".txt");
        // WHEN
        UUID jobId = taskService.createTask(command);
        // THEN
        TaskStatus initialStatus = taskService.getTaskStatus(jobId);
        Assertions.assertEquals(TaskStatus.Running, initialStatus);
        // WHEN
        Thread.sleep(1200);
        // THEN
        TaskStatus finalStatus = taskService.getTaskStatus(jobId);
        Assertions.assertEquals(TaskStatus.Completed, finalStatus);
    }

    @Test
    void shouldReturnTaskWhenItExists() {
        // GIVEN
        Task task = new Task();
        Task saved = taskRepository.save(task);
        UUID jobId = saved.getTaskId();
        // WHEN
        Task result = taskService.getTask(jobId);
        // THEN
        Assertions.assertNotNull(result);
        Assertions.assertEquals(jobId, result.getTaskId());
    }

    @Test
    void shouldThrowExceptionWhenTaskDoesNotExist() {
        // GIVEN
        UUID nonExistentId = UUID.randomUUID();
        // THEN
        Assertions.assertThrows(java.util.NoSuchElementException.class, () -> {
            taskService.getTask(nonExistentId);
        });
    }
}
