package kg.astra.replan;

import kg.astra.domain.TaskType;

import java.time.Instant;

public record AssignmentDto(String taskId, String taskName, TaskType taskType,
                            String crewId, String crewName, Instant start, Instant end) {}
