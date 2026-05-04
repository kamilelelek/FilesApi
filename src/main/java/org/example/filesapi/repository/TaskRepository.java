package org.example.filesapi.repository;

import org.example.filesapi.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    boolean existsById(UUID jobId);
}
