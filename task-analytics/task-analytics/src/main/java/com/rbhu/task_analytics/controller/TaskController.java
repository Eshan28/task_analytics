package com.rbhu.task_analytics.controller;



import com.rbhu.task_analytics.dto.AnalyticsResponse;
import com.rbhu.task_analytics.dto.TaskRequest;
import com.rbhu.task_analytics.dto.TaskResponse;
import com.rbhu.task_analytics.dto.TaskUpdateRequest;
import com.rbhu.task_analytics.model.Task;
import com.rbhu.task_analytics.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;


    @PostMapping("/tasks")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/tasks")
    public ResponseEntity<List<TaskResponse>> getAllTasks(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Task.TaskStatus status) {
        return ResponseEntity.ok(taskService.getAllTasks(category, status));
    }


    @GetMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }


    @PutMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @RequestBody TaskUpdateRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }


    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsResponse> getAnalytics() {
        return ResponseEntity.ok(taskService.getAnalytics());
    }
}