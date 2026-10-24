package smart_todo_api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import smart_todo_api.entity.Task;
import smart_todo_api.entity.TaskPriority;
import smart_todo_api.entity.TaskStatus;
import smart_todo_api.repository.TaskRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void cleanDatabase() {
        taskRepository.deleteAll();
    }

    private Task save(String title, TaskPriority priority, TaskStatus status) {
        Task task = new Task();
        task.setTitle(title);
        task.setPriority(priority);
        task.setStatus(status);
        return taskRepository.save(task);
    }

    @Test
    void createTask_returns201WithDefaults() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Learn Spring Boot","priority":"HIGH","dueDate":"2026-11-01"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Learn Spring Boot"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void createTask_blankTitle_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"","priority":"HIGH"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").value("Title is required"));
    }

    @Test
    void createTask_invalidPriority_returns400() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Test","priority":"URGENT"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTask_whenMissing_returns404() throws Exception {
        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id 999"));
    }

    @Test
    void searchAndFilter_returnOnlyMatchingTasks() throws Exception {
        save("Learn Spring Boot", TaskPriority.HIGH, TaskStatus.ONGOING);
        save("Write README", TaskPriority.LOW, TaskStatus.TODO);

        mockMvc.perform(get("/api/tasks").param("keyword", "spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Learn Spring Boot"));

        mockMvc.perform(get("/api/tasks").param("status", "TODO").param("priority", "LOW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Write README"));
    }

    @Test
    void updateStatus_thenStatsReflectIt() throws Exception {
        Task task = save("Ship project", TaskPriority.MEDIUM, TaskStatus.TODO);

        mockMvc.perform(patch("/api/tasks/{id}/status", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"COMPLETED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(get("/api/tasks/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.pending").value(0))
                .andExpect(jsonPath("$.completed").value(1));
    }

    @Test
    void deleteTask_returns204ThenTaskIsGone() throws Exception {
        Task task = save("Temporary", TaskPriority.LOW, TaskStatus.TODO);

        mockMvc.perform(delete("/api/tasks/{id}", task.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/{id}", task.getId()))
                .andExpect(status().isNotFound());
    }
}