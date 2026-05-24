package com.rbhu.task_analytics.agent;



import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.rbhu.task_analytics.dto.ChatRequest;
import com.rbhu.task_analytics.dto.ChatResponse;
import com.rbhu.task_analytics.model.ChatMessage;
import com.rbhu.task_analytics.repository.ChatMessageRepository;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AgentService {

    private final ChatMessageRepository chatMessageRepository;
    private final ToolDefinitions toolDefinitions;
    private final ToolExecutor toolExecutor;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    public AgentService(ChatMessageRepository chatMessageRepository,
                        ToolDefinitions toolDefinitions,
                        ToolExecutor toolExecutor,
                        ObjectMapper objectMapper) {
        this.chatMessageRepository = chatMessageRepository;
        this.toolDefinitions = toolDefinitions;
        this.toolExecutor = toolExecutor;
        this.objectMapper = objectMapper;
    }

    public ChatResponse chat(ChatRequest request) throws Exception {

        chatMessageRepository.save(ChatMessage.builder()
                .sessionId(request.getSessionId())
                .role(ChatMessage.MessageRole.user)
                .content(request.getMessage())
                .build());

        List<ChatMessage> history = chatMessageRepository
                .findBySessionIdOrderByCreatedAtAsc(request.getSessionId());

        List<Map<String, Object>> executedActions = new ArrayList<>();
        String finalReply = runAgentLoop(history, executedActions);

        chatMessageRepository.save(ChatMessage.builder()
                .sessionId(request.getSessionId())
                .role(ChatMessage.MessageRole.assistant)
                .content(finalReply)
                .build());

        return ChatResponse.builder()
                .reply(finalReply)
                .actions(executedActions)
                .build();
    }

    private String runAgentLoop(List<ChatMessage> history,
                                List<Map<String, Object>> actions) throws Exception {
        int maxIterations = 10;
        int iteration = 0;

        ArrayNode messages = buildMessages(history);

        while (iteration < maxIterations) {
            iteration++;

            String responseBody = callGroqApi(messages);
            JsonNode response = objectMapper.readTree(responseBody);

            if (response.has("error")) {
                throw new RuntimeException("Groq API error: " +
                        response.get("error").get("message").asText());
            }

            JsonNode choices = response.get("choices");
            if (choices == null || choices.isEmpty()) {
                return "I could not process your request.";
            }

            JsonNode choice = choices.get(0);
            JsonNode message = choice.get("message");
            String finishReason = choice.get("finish_reason").asText();

            if (!"tool_calls".equals(finishReason)) {
                JsonNode content = message.get("content");
                if (content != null && !content.isNull()) {
                    return content.asText();
                }
                return "I completed the requested actions.";
            }

            messages.add(message);

            JsonNode toolCalls = message.get("tool_calls");
            for (JsonNode toolCall : toolCalls) {
                String toolCallId = toolCall.get("id").asText();
                String toolName = toolCall.get("function").get("name").asText();
                String argsString = toolCall.get("function").get("arguments").asText();

                JsonNode toolArgs = objectMapper.readTree(argsString);

                log.info("Executing tool: {} args: {}", toolName, toolArgs);

                String result = toolExecutor.execute(toolName, toolArgs);

                Map<String, Object> action = new HashMap<>();
                action.put("tool", toolName);
                action.put("args", argsString);
                action.put("result", result);
                actions.add(action);

                ObjectNode toolResultMsg = objectMapper.createObjectNode();
                toolResultMsg.put("role", "tool");
                toolResultMsg.put("tool_call_id", toolCallId);
                toolResultMsg.put("content", result);
                messages.add(toolResultMsg);
            }
        }

        return "I was unable to complete the request after multiple attempts.";
    }

    private ArrayNode buildMessages(List<ChatMessage> history) {
        ArrayNode messages = objectMapper.createArrayNode();

        ObjectNode systemMsg = objectMapper.createObjectNode();
        systemMsg.put("role", "system");
        systemMsg.put("content",
                "You are a personal productivity assistant. Help users manage tasks. " +
                        "RULES: " +
                        "1. Always use tools to read/modify real data — never invent information. " +
                        "2. When user asks to see tasks or analytics, call the tool first then respond. " +
                        "3. Parse natural dates: tomorrow=" + java.time.LocalDate.now().plusDays(1) +
                        ", today=" + java.time.LocalDate.now() + ". " +
                        "4. If request is ambiguous, ask a clarifying question. " +
                        "5. After actions, give a concise summary with insights from real data."
        );
        messages.add(systemMsg);

        for (ChatMessage msg : history) {
            if (msg.getContent() != null && !msg.getContent().isEmpty()) {
                ObjectNode message = objectMapper.createObjectNode();
                message.put("role", msg.getRole().name());
                message.put("content", msg.getContent());
                messages.add(message);
            }
        }
        return messages;
    }

    private String callGroqApi(ArrayNode messages) throws Exception {

        ArrayNode tools = buildGroqTools();

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        requestBody.put("max_tokens", 4096);
        requestBody.put("temperature", 0.1);
        requestBody.set("messages", messages);
        requestBody.set("tools", tools);
        requestBody.put("tool_choice", "auto");

        String json = objectMapper.writeValueAsString(requestBody);

        RequestBody body = RequestBody.create(
                json, MediaType.get("application/json"));

        Request httpRequest = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            if (!response.isSuccessful()) {
                log.error("Groq API error {}: {}", response.code(), responseBody);
                throw new RuntimeException("Groq API error: " + responseBody);
            }
            return responseBody;
        }
    }

    private ArrayNode buildGroqTools() {
        ArrayNode tools = objectMapper.createArrayNode();

        tools.add(buildTool("create_task",
                "Creates a new task. Use when user wants to add or schedule a task. Parse natural language dates into YYYY-MM-DD.",
                new String[][]{
                        {"title", "string", "The task title", "true"},
                        {"category", "string", "Category: Work, Health, Learning, or Other", "false"},
                        {"due_date", "string", "Due date in YYYY-MM-DD format", "false"}
                }));

        tools.add(buildTool("get_tasks",
                "Fetches tasks. Can filter by category and/or status.",
                new String[][]{
                        {"category", "string", "Filter by category. Optional.", "false"},
                        {"status", "string", "Filter by status: pending or completed. Optional.", "false"}
                }));

        tools.add(buildTool("update_task",
                "Updates a task. Use to mark complete or edit. Requires task id.",
                new String[][]{
                        {"id", "integer", "The task ID to update", "true"},
                        {"status", "string", "New status: pending or completed", "false"},
                        {"title", "string", "New title", "false"},
                        {"category", "string", "New category", "false"}
                }));

        tools.add(buildTool("delete_task",
                "Deletes a task permanently.",
                new String[][]{
                        {"id", "integer", "The task ID to delete", "true"}
                }));

        tools.add(buildTool("get_analytics",
                "Fetches analytics: total tasks, completion rate, category breakdown.",
                new String[][]{}));

        tools.add(buildTool("mark_all_completed_by_category",
                "Marks ALL tasks in a category as completed.",
                new String[][]{
                        {"category", "string", "The category to mark all completed", "true"}
                }));

        return tools;
    }

    private ObjectNode buildTool(String name, String description, String[][] params) {
        ObjectNode tool = objectMapper.createObjectNode();
        tool.put("type", "function");

        ObjectNode function = objectMapper.createObjectNode();
        function.put("name", name);
        function.put("description", description);

        ObjectNode parameters = objectMapper.createObjectNode();
        parameters.put("type", "object");
        ObjectNode properties = objectMapper.createObjectNode();
        ArrayNode required = objectMapper.createArrayNode();

        for (String[] param : params) {
            ObjectNode prop = objectMapper.createObjectNode();
            prop.put("type", param[1]);
            prop.put("description", param[2]);
            properties.set(param[0], prop);
            if ("true".equals(param[3])) {
                required.add(param[0]);
            }
        }

        parameters.set("properties", properties);
        if (required.size() > 0) {
            parameters.set("required", required);
        }
        function.set("parameters", parameters);
        tool.set("function", function);
        return tool;
    }

    public void clearHistory(String sessionId) {
        chatMessageRepository.deleteBySessionId(sessionId);
    }
}