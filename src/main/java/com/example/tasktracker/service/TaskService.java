package com.example.tasktracker.service;

import com.example.tasktracker.dto.OverdueStatusSummary;
import com.example.tasktracker.dto.TaskRequest;
import com.example.tasktracker.dto.TaskResponse;
import com.example.tasktracker.model.TaskStatus;

import java.util.List;

public interface TaskService {

    TaskResponse createTask(TaskRequest request);

    TaskResponse getTaskById(Long id);

    List<TaskResponse> getAllTasks();

    List<TaskResponse> getTasksByStatus(TaskStatus status);

    TaskResponse updateTask(Long id, TaskRequest request);

    void deleteTask(Long id);

    List<OverdueStatusSummary> getOverdueSummary();
}
