package com.rbhu.task_analytics.dto;



import com.fasterxml.jackson.annotation.JsonProperty;
import com.rbhu.task_analytics.model.Task;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskUpdateRequest {

    @JsonProperty("title")
    private String title;

    @JsonProperty("category")
    private String category;

    @JsonProperty("status")
    private Task.TaskStatus status;

    @JsonProperty("dueDate")
    private LocalDate dueDate;
}