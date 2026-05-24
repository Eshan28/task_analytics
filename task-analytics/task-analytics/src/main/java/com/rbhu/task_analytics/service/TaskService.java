package com.rbhu.task_analytics.service;



import com.rbhu.task_analytics.dto.AnalyticsResponse;
import com.rbhu.task_analytics.dto.TaskRequest;
import com.rbhu.task_analytics.dto.TaskResponse;
import com.rbhu.task_analytics.dto.TaskUpdateRequest;
import com.rbhu.task_analytics.model.Task;
import com.rbhu.task_analytics.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;


    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        Task task = Task.builder()
                .title(request.getTitle())
                .category(request.getCategory() != null ? request.getCategory() : "Other")
                .status(Task.TaskStatus.pending)
                .dueDate(request.getDueDate())
                .build();

        Task saved = taskRepository.save(task);
        return TaskResponse.from(saved);
    }


    public List<TaskResponse> getAllTasks(String category, Task.TaskStatus status) {
        List<Task> tasks;

        if (category != null && status != null) {
            tasks = taskRepository.findByCategoryAndStatus(category, status);
        } else if (category != null) {
            tasks = taskRepository.findByCategory(category);
        } else if (status != null) {
            tasks = taskRepository.findByStatus(status);
        } else {
            tasks = taskRepository.findAll();
        }

        return tasks.stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }


    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        return TaskResponse.from(task);
    }


    @Transactional
    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getCategory() != null) task.setCategory(request.getCategory());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());

        Task updated = taskRepository.save(task);
        return TaskResponse.from(updated);
    }


    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }


    public AnalyticsResponse getAnalytics() {
        long total = taskRepository.count();
        long completed = taskRepository.countCompleted();
        long pending = total - completed;


        double completionRate = total > 0 ? (completed * 100.0 / total) : 0.0;


        Map<String, Long> categoryBreakdown = new HashMap<>();
        List<Object[]> rawCounts = taskRepository.countByCategory();
        for (Object[] row : rawCounts) {
            String cat = (String) row[0];
            Long count = (Long) row[1];
            categoryBreakdown.put(cat, count);
        }

        return AnalyticsResponse.builder()
                .totalTasks(total)
                .completedTasks(completed)
                .pendingTasks(pending)
                .completionRate(Math.round(completionRate * 100.0) / 100.0)
                .categoryBreakdown(categoryBreakdown)
                .build();
    }


    public List<TaskResponse> searchByKeyword(String keyword) {
        return taskRepository.findByTitleContainingIgnoreCase(keyword)
                .stream().map(TaskResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public int markAllCompletedByCategory(String category) {
        return taskRepository.markAllCompletedByCategory(category);
    }
}