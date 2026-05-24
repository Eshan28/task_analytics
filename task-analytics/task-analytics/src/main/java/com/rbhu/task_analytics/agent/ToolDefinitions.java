package com.rbhu.task_analytics.agent;



import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

@Component
public class ToolDefinitions {

    private final ObjectMapper mapper;

    public ToolDefinitions(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public ArrayNode getTools() {
        ArrayNode tools = mapper.createArrayNode();
        tools.add(createTaskTool());
        tools.add(getTasksTool());
        tools.add(updateTaskTool());
        tools.add(deleteTaskTool());
        tools.add(getAnalyticsTool());
        tools.add(markAllCompletedByCategoryTool());
        return tools;
    }

    private ObjectNode createTaskTool() {
        ObjectNode tool = mapper.createObjectNode();
        tool.put("name", "create_task");
        tool.put("description",
                "Creates a new task. Use this when user wants to add, create, or schedule a task. " +
                        "Parse natural language dates like 'tomorrow', 'next Monday', 'Friday' into YYYY-MM-DD format.");

        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode props = mapper.createObjectNode();

        ObjectNode title = mapper.createObjectNode();
        title.put("type", "string");
        title.put("description", "The task title");
        props.set("title", title);

        ObjectNode category = mapper.createObjectNode();
        category.put("type", "string");
        category.put("description", "Category: Work, Health, Learning, or Other");
        props.set("category", category);

        ObjectNode dueDate = mapper.createObjectNode();
        dueDate.put("type", "string");
        dueDate.put("description", "Due date in YYYY-MM-DD format. Optional.");
        props.set("due_date", dueDate);

        schema.set("properties", props);
        ArrayNode required = mapper.createArrayNode();
        required.add("title");
        schema.set("required", required);

        tool.set("input_schema", schema);
        return tool;
    }

    private ObjectNode getTasksTool() {
        ObjectNode tool = mapper.createObjectNode();
        tool.put("name", "get_tasks");
        tool.put("description",
                "Fetches tasks. Can filter by category and/or status. " +
                        "Use this when user asks to see, list, or show their tasks.");

        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode props = mapper.createObjectNode();

        ObjectNode category = mapper.createObjectNode();
        category.put("type", "string");
        category.put("description", "Filter by category. Optional.");
        props.set("category", category);

        ObjectNode status = mapper.createObjectNode();
        status.put("type", "string");
        status.put("description", "Filter by status: pending or completed. Optional.");
        props.set("status", status);

        schema.set("properties", props);
        schema.set("required", mapper.createArrayNode());

        tool.set("input_schema", schema);
        return tool;
    }

    private ObjectNode updateTaskTool() {
        ObjectNode tool = mapper.createObjectNode();
        tool.put("name", "update_task");
        tool.put("description",
                "Updates an existing task. Use to mark complete, change title, category or due date. " +
                        "You must provide the task id.");

        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode props = mapper.createObjectNode();

        ObjectNode id = mapper.createObjectNode();
        id.put("type", "integer");
        id.put("description", "The task ID to update");
        props.set("id", id);

        ObjectNode status = mapper.createObjectNode();
        status.put("type", "string");
        status.put("description", "New status: pending or completed");
        props.set("status", status);

        ObjectNode title = mapper.createObjectNode();
        title.put("type", "string");
        title.put("description", "New title. Optional.");
        props.set("title", title);

        ObjectNode category = mapper.createObjectNode();
        category.put("type", "string");
        category.put("description", "New category. Optional.");
        props.set("category", category);

        schema.set("properties", props);
        ArrayNode required = mapper.createArrayNode();
        required.add("id");
        schema.set("required", required);

        tool.set("input_schema", schema);
        return tool;
    }

    private ObjectNode deleteTaskTool() {
        ObjectNode tool = mapper.createObjectNode();
        tool.put("name", "delete_task");
        tool.put("description",
                "Deletes a task permanently. Use when user says remove, delete, or cancel a task.");

        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode props = mapper.createObjectNode();

        ObjectNode id = mapper.createObjectNode();
        id.put("type", "integer");
        id.put("description", "The task ID to delete");
        props.set("id", id);

        schema.set("properties", props);
        ArrayNode required = mapper.createArrayNode();
        required.add("id");
        schema.set("required", required);

        tool.set("input_schema", schema);
        return tool;
    }

    private ObjectNode getAnalyticsTool() {
        ObjectNode tool = mapper.createObjectNode();
        tool.put("name", "get_analytics");
        tool.put("description",
                "Fetches productivity analytics: total tasks, completion rate, category breakdown. " +
                        "Use when user asks how they are doing, their progress, stats, or productivity.");

        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");
        schema.set("properties", mapper.createObjectNode());
        schema.set("required", mapper.createArrayNode());

        tool.set("input_schema", schema);
        return tool;
    }

    private ObjectNode markAllCompletedByCategoryTool() {
        ObjectNode tool = mapper.createObjectNode();
        tool.put("name", "mark_all_completed_by_category");
        tool.put("description",
                "Marks ALL tasks in a specific category as completed at once. " +
                        "Use when user says 'mark all Work tasks done' or 'complete all Health tasks'.");

        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode props = mapper.createObjectNode();

        ObjectNode category = mapper.createObjectNode();
        category.put("type", "string");
        category.put("description", "The category name to mark all as completed");
        props.set("category", category);

        schema.set("properties", props);
        ArrayNode required = mapper.createArrayNode();
        required.add("category");
        schema.set("required", required);

        tool.set("input_schema", schema);
        return tool;
    }
}