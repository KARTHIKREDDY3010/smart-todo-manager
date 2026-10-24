package smart_todo_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import smart_todo_api.entity.TaskPriority;
import smart_todo_api.entity.TaskStatus;

import java.time.LocalDate;

public record TaskRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must be at most 100 characters")
        String title,

        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description,

        TaskPriority priority,

        LocalDate dueDate,

        TaskStatus status
) {
}