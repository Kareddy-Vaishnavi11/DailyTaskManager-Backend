package com.example.dailytaskmanager.service;

import com.example.dailytaskmanager.entity.Task;
import com.example.dailytaskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks(String status) {
        if (status != null && !status.trim().isEmpty()) {
            String normalizedStatus = normalizeStatus(status);
            return taskRepository.findByStatusIgnoreCaseOrderByIdDesc(normalizedStatus);
        }
        return taskRepository.findAllByOrderByIdDesc();
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with id: " + id));
    }

    public Task createTask(Task task) {
        validateTitle(task.getTitle());

        if (task.getStatus() == null || task.getStatus().trim().isEmpty()) {
            task.setStatus("Pending");
        } else {
            task.setStatus(normalizeStatus(task.getStatus()));
        }

        return taskRepository.save(task);
    }

    public Task updateTask(Long id, Task taskDetails) {
        Task existingTask = getTaskById(id);

        validateTitle(taskDetails.getTitle());

        existingTask.setTitle(taskDetails.getTitle().trim());
        existingTask.setDescription(taskDetails.getDescription());
        existingTask.setDueDate(taskDetails.getDueDate());

        if (taskDetails.getStatus() != null && !taskDetails.getStatus().trim().isEmpty()) {
            existingTask.setStatus(normalizeStatus(taskDetails.getStatus()));
        }

        return taskRepository.save(existingTask);
    }

    public Task updateStatus(Long id, String requestedStatus) {
        Task existingTask = getTaskById(id);

        if (requestedStatus != null && !requestedStatus.trim().isEmpty()) {
            existingTask.setStatus(normalizeStatus(requestedStatus));
        } else {
            // Toggle status if not explicitly provided
            if ("Pending".equalsIgnoreCase(existingTask.getStatus())) {
                existingTask.setStatus("Completed");
            } else {
                existingTask.setStatus("Pending");
            }
        }

        return taskRepository.save(existingTask);
    }

    public void deleteTask(Long id) {
        Task task = getTaskById(id);
        taskRepository.delete(task);
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task title is required and cannot be blank");
        }
    }

    private String normalizeStatus(String status) {
        String trimmed = status.trim();
        if ("Pending".equalsIgnoreCase(trimmed)) {
            return "Pending";
        } else if ("Completed".equalsIgnoreCase(trimmed)) {
            return "Completed";
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: '" + status + "'. Status must be either 'Pending' or 'Completed'");
        }
    }
}
