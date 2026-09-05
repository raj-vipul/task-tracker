package com.example.tasktracker.service;

import com.example.tasktracker.dto.TaskRequest;
import com.example.tasktracker.dto.TaskResponse;
import com.example.tasktracker.exception.ResourceNotFoundException;
import com.example.tasktracker.mapper.TaskMapper;
import com.example.tasktracker.model.Task;
import com.example.tasktracker.model.TaskStatus;
import com.example.tasktracker.repository.TaskRepository;
import com.example.tasktracker.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the service layer. The repository and mapper are mocked so
 * these tests run fast and in isolation, with no real database involved.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task existingTask;
    private TaskRequest request;

    @BeforeEach
    void setUp() {
        existingTask = new Task("Write unit tests", "Cover the service layer", TaskStatus.TODO,
                LocalDate.now().plusDays(3));
        existingTask.setId(1L);

        request = new TaskRequest();
        request.setTitle("Write unit tests");
        request.setDescription("Cover the service layer");
        request.setStatus(TaskStatus.TODO);
        request.setDueDate(LocalDate.now().plusDays(3));
    }

    @Test
    void createTask_savesAndReturnsMappedResponse() {
        Task unsaved = new Task(request.getTitle(), request.getDescription(),
                request.getStatus(), request.getDueDate());
        TaskResponse expectedResponse = new TaskResponse(1L, request.getTitle(),
                request.getDescription(), request.getStatus(), request.getDueDate(), null);

        when(taskMapper.toEntity(request)).thenReturn(unsaved);
        when(taskRepository.save(unsaved)).thenReturn(existingTask);
        when(taskMapper.toResponse(existingTask)).thenReturn(expectedResponse);

        TaskResponse result = taskService.createTask(request);

        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(expectedResponse.getTitle(), result.getTitle());
        verify(taskRepository, times(1)).save(unsaved);
    }

    @Test
    void getTaskById_whenFound_returnsMappedResponse() {
        TaskResponse expectedResponse = new TaskResponse(1L, existingTask.getTitle(),
                existingTask.getDescription(), existingTask.getStatus(), existingTask.getDueDate(), null);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskMapper.toResponse(existingTask)).thenReturn(expectedResponse);

        TaskResponse result = taskService.getTaskById(1L);

        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(expectedResponse.getTitle(), result.getTitle());
    }

    @Test
    void getTaskById_whenMissing_throwsResourceNotFoundException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(99L));
        verify(taskMapper, never()).toResponse(any());
    }

    @Test
    void getTasksByStatus_returnsOnlyMatchingTasks() {
        when(taskRepository.findByStatus(TaskStatus.TODO)).thenReturn(List.of(existingTask));
        when(taskMapper.toResponse(existingTask)).thenReturn(
                new TaskResponse(1L, existingTask.getTitle(), existingTask.getDescription(),
                        TaskStatus.TODO, existingTask.getDueDate(), null));

        List<TaskResponse> results = taskService.getTasksByStatus(TaskStatus.TODO);

        assertEquals(1, results.size());
        assertEquals(TaskStatus.TODO, results.get(0).getStatus());
    }

    @Test
    void updateTask_whenFound_updatesAndSaves() {
        request.setStatus(TaskStatus.IN_PROGRESS);
        TaskResponse expectedResponse = new TaskResponse(1L, existingTask.getTitle(),
                existingTask.getDescription(), TaskStatus.IN_PROGRESS, existingTask.getDueDate(), null);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);
        when(taskMapper.toResponse(existingTask)).thenReturn(expectedResponse);

        TaskResponse result = taskService.updateTask(1L, request);

        verify(taskMapper).updateEntity(existingTask, request);
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
    }

    @Test
    void deleteTask_whenMissing_throwsResourceNotFoundException() {
        when(taskRepository.existsById(42L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(42L));
        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    void deleteTask_whenFound_deletesSuccessfully() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        taskService.deleteTask(1L);

        verify(taskRepository, times(1)).deleteById(1L);
    }
}
