package com.example.tasktracker.repository;

import com.example.tasktracker.dto.OverdueStatusSummary;
import com.example.tasktracker.model.Task;
import com.example.tasktracker.model.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Uses an in-memory H2 database (PostgreSQL compatibility mode) so the native
 * CTE query can be exercised without a real Postgres instance running.
 */
@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @org.springframework.beans.factory.annotation.Autowired
    private TaskRepository taskRepository;

    @Test
    void getOverdueTaskCountsByStatus_groupsOnlyPastDueUnfinishedTasks() {
        taskRepository.save(new Task("Overdue todo", "d", TaskStatus.TODO, LocalDate.now().minusDays(2)));
        taskRepository.save(new Task("Overdue in progress", "d", TaskStatus.IN_PROGRESS, LocalDate.now().minusDays(1)));
        taskRepository.save(new Task("Overdue but done", "d", TaskStatus.DONE, LocalDate.now().minusDays(5)));
        taskRepository.save(new Task("Not due yet", "d", TaskStatus.TODO, LocalDate.now().plusDays(3)));

        List<OverdueStatusSummary> summary = taskRepository.getOverdueTaskCountsByStatus();

        assertEquals(2, summary.size());
        assertTrue(summary.stream().anyMatch(s -> "TODO".equals(s.getStatus()) && s.getTaskCount() == 1));
        assertTrue(summary.stream().anyMatch(s -> "IN_PROGRESS".equals(s.getStatus()) && s.getTaskCount() == 1));
    }

    @Test
    void findByStatus_returnsOnlyMatchingTasks() {
        taskRepository.save(new Task("A", "d", TaskStatus.TODO, LocalDate.now()));
        taskRepository.save(new Task("B", "d", TaskStatus.DONE, LocalDate.now()));

        List<Task> todos = taskRepository.findByStatus(TaskStatus.TODO);

        assertEquals(1, todos.size());
        assertEquals("A", todos.get(0).getTitle());
    }
}
