package org.example.filesapi;

import org.example.filesapi.model.CreateTaskCommand;
import org.example.filesapi.model.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class FilesApiApplicationTests {
@Test
    void shouldFindFilesWithSpecificExtension(@TempDir Path tempDir) throws IOException, InterruptedException {
    // GIVEN: happy path
    Files.createFile(tempDir.resolve("test1.txt"));
    Files.createFile(tempDir.resolve("test2.txt"));
    Files.createFile(tempDir.resolve("image.jpg"));

    CreateTaskCommand command = new CreateTaskCommand();
    command.setSource(tempDir.toString());
    command.setExtension(".txt");
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
