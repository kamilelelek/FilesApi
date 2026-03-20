package org.example.filesapi.repository;

import org.example.filesapi.model.Task;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TaskRepository {
    private final AtomicLong counter = new AtomicLong(0); //responsible for generate the ID for current task. AtomicLong increment the vaule and cannot be replaced or smth like that
    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
    public void addTask(Task task) {
        tasks.put(task.getTaskId(),task);
    }
    public Task getById(Long taskId) {
        return tasks.get(taskId);
    }
    public Long generateId() {
        return counter.incrementAndGet();
    }

}
