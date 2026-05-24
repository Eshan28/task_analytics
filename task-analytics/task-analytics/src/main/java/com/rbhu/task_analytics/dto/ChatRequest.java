package com.rbhu.task_analytics.dto;




import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {

    @NotBlank(message = "Message cannot be empty")
    @JsonProperty("message")
    private String message;

    @NotBlank(message = "Session ID is required")
    @JsonProperty("sessionId")
    private String sessionId;
}