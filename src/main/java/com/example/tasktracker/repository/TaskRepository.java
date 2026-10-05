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
