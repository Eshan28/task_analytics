package com.rbhu.task_analytics.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {

    private long totalTasks;
    private long completedTasks;
    private long pendingTasks;
    private double completionRate;
    private Map<String, Long> categoryBreakdown;
}