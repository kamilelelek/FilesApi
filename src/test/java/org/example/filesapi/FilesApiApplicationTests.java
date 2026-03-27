package org.example.filesapi;

import org.example.filesapi.model.CreateTaskCommand;
import org.example.filesapi.repository.TaskRepository;
import org.example.filesapi.service.TaskService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class FilesApiApplicationTests {
@Test
    void shouldFindFilesWithSpecificExtension(@TempDir Path tempDir) throws IOException, InterruptedException {
    // GIVEN: happy path
    Files.createFile(tempDir.resolve("test1.txt"));
    Files.createFile(tempDir.resolve("test2.txt"));
    Files.createFile(tempDir.resolve("test3.txt"));
    //when
    CreateTaskCommand command = new CreateTaskCommand();
    TaskRepository taskRepository = new TaskRepository();
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    TaskService taskSevice= new TaskService(taskRepository, executorService);
    command.setSource(tempDir.toString());
    command.setExtension(".txt");
    List<File> result = taskSevice.searchFilesWithExtension(command);

    //then
    Assertions.assertEquals(3, result.size());
}



   @Test
    void shouldThrowExceptionWhenNoFilesFound(@TempDir Path tempDir) {
       // GIVEN: empty folder
   }


    void shouldThrowExceptionWhenDirectoryDoesNotExist() {
        // GIVEN: no such direction
//TODO tomorrow write test
    }
}
