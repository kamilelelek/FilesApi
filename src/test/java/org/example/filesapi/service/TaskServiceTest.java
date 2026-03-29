package org.example.filesapi.service;

import org.example.filesapi.model.CreateTaskCommand;
import org.example.filesapi.model.Task;
import org.example.filesapi.model.TaskStatus;
import org.example.filesapi.repository.TaskRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest {
    private TaskService taskService;
    private TaskRepository taskRepository;
    private CreateTaskCommand command;

    @BeforeEach
    void setUp() {
        taskRepository = new TaskRepository();
        taskService = new TaskService(taskRepository, Executors.newFixedThreadPool(4));
        command = new CreateTaskCommand();

    }

    @Test
    void shouldFindFilesWithSpecificExtension(@TempDir Path tempDir) throws IOException, InterruptedException {
        // GIVEN: happy path
        Files.createFile(tempDir.resolve("test1.txt"));
        Files.createFile(tempDir.resolve("test2.txt"));
        Files.createFile(tempDir.resolve("test3.txt"));
        //when
        command.setSource(tempDir.toString());
        command.setExtension(".txt");
        List<File> result = taskService.searchFilesWithExtension(command);

        //then
        Assertions.assertEquals(3, result.size());
    }

    @Test
    void emptyFolder(@TempDir Path tempDir) throws InterruptedException {
        //GIVEN
        assertTrue(Files.exists(tempDir), "JUnit TempDir should exist");
        //WHEN
        command.setSource(tempDir.toAbsolutePath().toString());
        command.setExtension(".txt");
        List<File> result = taskService.searchFilesWithExtension(command);
        //THEN
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    void wrongPath() throws InterruptedException {
        //GIVEN
        command.setSource("text.txt");
        command.setExtension("text.txt");
        //THEN
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
        //when
        command.setSource(tempDir.toString());
        command.setExtension(".Txt");
        List<File> result = taskService.searchFilesWithExtension(command);
        //then
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
        Long taskId = service.runTask(command);
        // THEN
        Thread.sleep(1500);

        Assertions.assertEquals(org.example.filesapi.model.TaskStatus.Completed, taskRepository.getById(taskId).getStatus());
        Assertions.assertFalse(taskRepository.getById(taskId).getResult().isEmpty());
    }

    @Test
    void shouldCreateTaskAndReturnStatus(@TempDir Path tempDir) throws InterruptedException {
        //GIVEN
        command.setSource(tempDir.toString());
        command.setExtension(".txt");

        //WHEN
        long jobId = taskService.createTask(command);

        //THEN
        TaskStatus initialStatus = taskService.getTaskStatus(jobId);
        Assertions.assertEquals(TaskStatus.Running, initialStatus);

        //WHEN
        Thread.sleep(1200);

        //THEN
        TaskStatus finalStatus = taskService.getTaskStatus(jobId);
        Assertions.assertEquals(TaskStatus.Completed, finalStatus);
    }

    @Test
    void shouldReturnTaskWhenItExists() {
        //GIVEN
        long jobId = taskRepository.generateId();
        Task task = new Task(jobId);
        taskRepository.addTask(task);

        //WHEN
        Task result = taskService.getTask(jobId);

        //THEN
        Assertions.assertNotNull(result);
        Assertions.assertEquals(jobId, result.getTaskId());
    }

    @Test
    void shouldThrowExceptionWhenTaskDoesNotExist() {
        //GIVEN
        Long nonExistentId = 999L;

        //THEN
        Assertions.assertThrows(java.util.NoSuchElementException.class, () -> {
            taskService.getTask(nonExistentId);
        });
    }
}
