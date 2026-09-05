package com.example.tasktracker.repository;

import com.example.tasktracker.dto.OverdueStatusSummary;
import com.example.tasktracker.model.Task;
import com.example.tasktracker.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(TaskStatus status);

    long countByStatus(TaskStatus status);

    /**
     * Example of the "SQL with CTEs" skill called out in the job description.
     * Builds a temporary result set of overdue tasks, then aggregates it by status.
     * Written as a native query so it runs the same way it would in a DB client.
     * Note: PostgreSQL folds unquoted identifiers to lower_snake_case, so the
     * alias below (task_count) is what Spring Data relaxed-binds to getTaskCount().
     */
    @Query(value = """
            WITH overdue_tasks AS (
                SELECT *
                FROM tasks
                WHERE due_date < CURRENT_DATE
                  AND status <> 'DONE'
            )
            SELECT status AS status, COUNT(*) AS task_count
            FROM overdue_tasks
            GROUP BY status
            ORDER BY task_count DESC
            """, nativeQuery = true)
    List<OverdueStatusSummary> getOverdueTaskCountsByStatus();
}
