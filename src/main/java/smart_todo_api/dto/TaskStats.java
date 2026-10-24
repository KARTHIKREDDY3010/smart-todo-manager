package smart_todo_api.dto;

public record TaskStats(long total, long pending, long ongoing, long completed) {
}