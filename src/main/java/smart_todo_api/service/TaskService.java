package smart_todo_api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smart_todo_api.dto.TaskRequest;
import smart_todo_api.dto.TaskResponse;
import smart_todo_api.dto.TaskStats;
import smart_todo_api.entity.Task;
import smart_todo_api.entity.TaskPriority;
import smart_todo_api.entity.TaskStatus;
import smart_todo_api.exception.ResourceNotFoundException;
import smart_todo_api.repository.TaskRepository;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> search(String keyword, TaskStatus status, TaskPriority priority) {
        String cleaned = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return taskRepository.search(cleaned, status, priority)
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(Long id) {
        return TaskResponse.from(findOrThrow(id));
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        Task task = new Task();
        applyRequest(task, request);
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = findOrThrow(id);
        applyRequest(task, request);
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateStatus(Long id, TaskStatus status) {
        Task task = findOrThrow(id);
        task.setStatus(status);
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void delete(Long id) {
        Task task = findOrThrow(id);
        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public TaskStats getStats() {
        return new TaskStats(
                taskRepository.count(),
                taskRepository.countByStatus(TaskStatus.TODO),
                taskRepository.countByStatus(TaskStatus.ONGOING),
                taskRepository.countByStatus(TaskStatus.COMPLETED)
        );
    }

    private Task findOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id));
    }

    private void applyRequest(Task task, TaskRequest request) {
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        if (request.status() != null) {
            task.setStatus(request.status());
        }
    }
}