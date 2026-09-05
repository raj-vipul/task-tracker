package com.example.tasktracker.controller;

import com.example.tasktracker.dto.TaskRequest;
import com.example.tasktracker.dto.TaskResponse;
import com.example.tasktracker.exception.ResourceNotFoundException;
import com.example.tasktracker.model.TaskStatus;
import com.example.tasktracker.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice test for the web layer only: the service is mocked, so this verifies
 * routing, request validation, status codes, and JSON serialization —
 * not business logic (that's covered in TaskServiceImplTest).
 */
@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @Test
    void createTask_withValidPayload_returns201() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle("Ship the API");
        request.setDescription("Deploy v1 of the task tracker");
        request.setStatus(TaskStatus.TODO);
        request.setDueDate(LocalDate.now().plusDays(7));

        TaskResponse response = new TaskResponse(1L, request.getTitle(), request.getDescription(),
                TaskStatus.TODO, request.getDueDate(), null);

        when(taskService.createTask(any(TaskRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Ship the API"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void createTask_withBlankTitle_returns400() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle("   ");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.title").exists());
    }

    @Test
    void getTask_whenExists_returns200() throws Exception {
        TaskResponse response = new TaskResponse(5L, "Existing task", "desc",
                TaskStatus.IN_PROGRESS, LocalDate.now(), null);

        when(taskService.getTaskById(5L)).thenReturn(response);

        mockMvc.perform(get("/api/tasks/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void getTask_whenMissing_returns404() throws Exception {
        when(taskService.getTaskById(404L))
                .thenThrow(new ResourceNotFoundException("Task not found with id: 404"));

        mockMvc.perform(get("/api/tasks/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id: 404"));
    }

    @Test
    void getTasks_filteredByStatus_callsServiceWithStatus() throws Exception {
        when(taskService.getTasksByStatus(eq(TaskStatus.DONE))).thenReturn(List.of());

        mockMvc.perform(get("/api/tasks").param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void deleteTask_whenExists_returns204() throws Exception {
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
    }
}
