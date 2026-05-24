package com.rbhu.task_analytics.agent;



import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.rbhu.task_analytics.dto.TaskRequest;
import com.rbhu.task_analytics.dto.TaskUpdateRequest;
import com.rbhu.task_analytics.model.Task;
import com.rbhu.task_analytics.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class ToolExecutor {

    private final TaskService taskService;
    private final ObjectMapper objectMapper;

    public ToolExecutor(TaskService taskService, ObjectMapper objectMapper) {
        this.taskService = taskService;
        this.objectMapper = objectMapper;
    }

    public String execute(String toolName, JsonNode args) {
        try {
            return switch (toolName) {
                case "create_task"                    -> createTask(args);
                case "get_tasks"                      -> getTasks(args);
                case "update_task"                    -> updateTask(args);
                case "delete_task"                    -> deleteTask(args);
                case "get_analytics"                  -> getAnalytics();
                case "mark_all_completed_by_category" -> markAllCompleted(args);
                default -> "Unknown tool: " + toolName;
            };
        } catch (Exception e) {
            log.error("Tool execution failed for {}: {}", toolName, e.getMessage());
            return "Error executing " + toolName + ": " + e.getMessage();
        }
    }

    private String createTask(JsonNode args) throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle(args.get("title").asText());
        request.setCategory(args.has("category") ? args.get("category").asText() : "Other");

        if (args.has("due_date") && !args.get("due_date").isNull()) {
            String dueDateStr = args.get("due_date").asText();
            if (!dueDateStr.isEmpty() && !dueDateStr.equals("null")) {
                request.setDueDate(LocalDate.parse(dueDateStr));
            }
        }

        var result = taskService.createTask(request);
        return "Task created: " + objectMapper.writeValueAsString(result);
    }

    private String getTasks(JsonNode args) throws Exception {
        String category = args.has("category") ? args.get("category").asText() : null;
        String statusStr = args.has("status")   ? args.get("status").asText()   : null;

        // Convert empty string to null
        if (category != null && category.isEmpty()) category = null;
        if (statusStr != null && statusStr.isEmpty()) statusStr = null;

        Task.TaskStatus status = null;
        if (statusStr != null) {
            status = Task.TaskStatus.valueOf(statusStr);
        }

        var result = taskService.getAllTasks(category, status);
        return "Tasks: " + objectMapper.writeValueAsString(result);
    }

    private String updateTask(JsonNode args) throws Exception {
        Long id = args.get("id").asLong();
        TaskUpdateRequest request = new TaskUpdateRequest();

        if (args.has("status") && !args.get("status").asText().isEmpty()) {
            request.setStatus(Task.TaskStatus.valueOf(args.get("status").asText()));
        }
        if (args.has("title") && !args.get("title").asText().isEmpty()) {
            request.setTitle(args.get("title").asText());
        }
        if (args.has("category") && !args.get("category").asText().isEmpty()) {
            request.setCategory(args.get("category").asText());
        }
        if (args.has("due_date") && !args.get("due_date").asText().isEmpty()) {
            request.setDueDate(LocalDate.parse(args.get("due_date").asText()));
        }

        var result = taskService.updateTask(id, request);
        return "Task updated: " + objectMapper.writeValueAsString(result);
    }

    private String deleteTask(JsonNode args) {
        Long id = args.get("id").asLong();
        taskService.deleteTask(id);
        return "Task with id " + id + " deleted successfully.";
    }

    private String getAnalytics() throws Exception {
        var result = taskService.getAnalytics();
        return "Analytics: " + objectMapper.writeValueAsString(result);
    }

    private String markAllCompleted(JsonNode args) {
        String category = args.get("category").asText();
        int count = taskService.markAllCompletedByCategory(category);
        return "Marked " + count + " tasks as completed in category: " + category;
    }
}