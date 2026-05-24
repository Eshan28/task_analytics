package com.rbhu.task_analytics.dto;



import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskRequest {

    @NotBlank(message = "Title is required")
    @JsonProperty("title")
    private String title;

    @JsonProperty("category")
    private String category = "Other";

    @JsonProperty("dueDate")
    private LocalDate dueDate;
}