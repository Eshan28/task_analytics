package com.rbhu.task_analytics.controller;



import com.rbhu.task_analytics.agent.AgentService;
import com.rbhu.task_analytics.dto.ChatRequest;
import com.rbhu.task_analytics.dto.ChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request) throws Exception {
        ChatResponse response = agentService.chat(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/history/{sessionId}")
    public ResponseEntity<Void> clearHistory(@PathVariable String sessionId) {
        agentService.clearHistory(sessionId);
        return ResponseEntity.noContent().build();
    }
}