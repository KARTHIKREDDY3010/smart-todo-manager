package smart_todo_api.dto;

import smart_todo_api.entity.Task;
import smart_todo_api.entity.TaskPriority;
import smart_todo_api.entity.TaskStatus;

import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskPriority priority,
        LocalDate dueDate,
        TaskStatus status
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getDueDate(),
                task.getStatus()
        );
    }
}