package smart_todo_api.repository;

import smart_todo_api.entity.Task;
import smart_todo_api.entity.TaskPriority;
import smart_todo_api.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    long countByStatus(TaskStatus status);

    @Query("""
            SELECT t FROM Task t
            WHERE (:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status IS NULL OR t.status = :status)
              AND (:priority IS NULL OR t.priority = :priority)
            ORDER BY t.id DESC
            """)
    List<Task> search(@Param("keyword") String keyword,
                      @Param("status") TaskStatus status,
                      @Param("priority") TaskPriority priority);
}
