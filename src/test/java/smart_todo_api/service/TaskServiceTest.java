package smart_todo_api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smart_todo_api.dto.TaskRequest;
import smart_todo_api.dto.TaskResponse;
import smart_todo_api.dto.TaskStats;
import smart_todo_api.entity.Task;
import smart_todo_api.entity.TaskPriority;
import smart_todo_api.entity.TaskStatus;
import smart_todo_api.exception.ResourceNotFoundException;
import smart_todo_api.repository.TaskRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void create_trimsTitleAndKeepsDefaults() {
        TaskRequest request = new TaskRequest("  Buy milk  ", null, null, null, null);
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        TaskResponse response = taskService.create(request);

        assertEquals(1L, response.id());
        assertEquals("Buy milk", response.title());
        assertEquals(TaskPriority.MEDIUM, response.priority());
        assertEquals(TaskStatus.TODO, response.status());
    }

    @Test
    void getById_whenMissing_throwsNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getById(99L));
    }

    @Test
    void updateStatus_changesStatus() {
        Task task = new Task();
        task.setId(5L);
        task.setTitle("Write tests");
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse response = taskService.updateStatus(5L, TaskStatus.COMPLETED);

        assertEquals(TaskStatus.COMPLETED, response.status());
    }

    @Test
    void delete_whenMissing_throwsAndDeletesNothing() {
        when(taskRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.delete(7L));
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void getStats_countsEachStatus() {
        when(taskRepository.count()).thenReturn(6L);
        when(taskRepository.countByStatus(TaskStatus.TODO)).thenReturn(3L);
        when(taskRepository.countByStatus(TaskStatus.ONGOING)).thenReturn(2L);
        when(taskRepository.countByStatus(TaskStatus.COMPLETED)).thenReturn(1L);

        TaskStats stats = taskService.getStats();

        assertEquals(6L, stats.total());
        assertEquals(3L, stats.pending());
        assertEquals(2L, stats.ongoing());
        assertEquals(1L, stats.completed());
    }

    @Test
    void search_blankKeywordIsTreatedAsNoKeyword() {
        when(taskRepository.search(null, null, null)).thenReturn(List.of());

        taskService.search("   ", null, null);

        verify(taskRepository).search(null, null, null);
    }
}