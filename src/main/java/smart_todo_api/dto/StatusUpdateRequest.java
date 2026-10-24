package smart_todo_api.dto;

import jakarta.validation.constraints.NotNull;
import smart_todo_api.entity.TaskStatus;

public record StatusUpdateRequest(
        @NotNull(message = "Status is required")
        TaskStatus status
) {
}