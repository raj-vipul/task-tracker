package com.example.tasktracker.dto;

/**
 * Projection interface used with a native CTE query in TaskRepository.
 * Spring Data JPA maps native query columns to these getters automatically.
 */
public interface OverdueStatusSummary {
    String getStatus();
    Long getTaskCount();
}
