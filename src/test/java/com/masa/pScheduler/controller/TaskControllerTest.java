package com.masa.pScheduler.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masa.pScheduler.dto.TaskCreateRequest;
import com.masa.pScheduler.dto.TaskResponse;
import com.masa.pScheduler.dto.TaskUpdateRequest;
import com.masa.pScheduler.model.Task;
import com.masa.pScheduler.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc
class TaskControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private TaskService taskService;
    
    @Test
    @WithMockUser(username = "testuser")
    void whenCreateTask_thenReturnCreated() throws Exception {
        // Given
        TaskCreateRequest request = TaskCreateRequest.builder()
                .title("New Task")
                .description("Description")
                .deadline(LocalDateTime.now().plusDays(1))
                .priority(Task.Priority.HIGH)
                .build();
        
        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("New Task")
                .status(Task.TaskStatus.PENDING)
                .build();
        
        when(taskService.createTask(any(TaskCreateRequest.class), anyString()))
                .thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/v1/tasks")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Task"));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void whenGetAllTasks_thenReturnTasks() throws Exception {
        // Given
        TaskResponse task1 = TaskResponse.builder().id(1L).title("Task 1").build();
        TaskResponse task2 = TaskResponse.builder().id(2L).title("Task 2").build();
        List<TaskResponse> tasks = Arrays.asList(task1, task2);
        
        when(taskService.getAllTasksForUser(anyString())).thenReturn(tasks);
        
        // When & Then
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[1].title").value("Task 2"));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void whenGetTaskById_thenReturnTask() throws Exception {
        // Given
        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .build();
        
        when(taskService.getTaskById(eq(1L), anyString())).thenReturn(response);
        
        // When & Then
        mockMvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void whenDeleteTask_thenReturnNoContent() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/tasks/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser")
    void whenUpdateTask_thenReturnUpdatedTask() throws Exception {
        // Given
        TaskUpdateRequest request = TaskUpdateRequest.builder()
                .title("Updated Task")
                .build();

        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("Updated Task")
                .build();

        when(taskService.updateTask(eq(1L), any(TaskUpdateRequest.class), anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/tasks/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void whenTitleTooLong_thenReturnBadRequest() throws Exception {
        String longTitle = "A".repeat(300); // exceeds 200 characters
        TaskUpdateRequest invalidRequest = TaskUpdateRequest.builder()
                .title(longTitle)
                .build();

        mockMvc.perform(put("/api/v1/tasks/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void whenDescriptionTooLong_thenReturnBadRequest() throws Exception {
        // Given
        String longDescription = "A".repeat(2100); // exceeds 2000 characters
        TaskUpdateRequest invalidRequest = TaskUpdateRequest.builder()
                .description(longDescription)
                .build();

        // When & Then
        mockMvc.perform(put("/api/v1/tasks/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void whenMarkTaskAsCompleted_thenReturnCompletedTask() throws Exception {
        // Given
        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .status(Task.TaskStatus.COMPLETED)
                .build();

        when(taskService.markTaskAsCompleted(eq(1L), anyString())).thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/api/v1/tasks/1/complete")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void whenTaskAlreadyCompleted_thenReturn200WithCurrentState() throws Exception {
        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .status(Task.TaskStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .build();

        when(taskService.markTaskAsCompleted(eq(1L), anyString())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/tasks/1/complete")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }


    @Test
    @WithMockUser(username = "unauthorizedUser")
    void whenMarkTaskAsCompleted_forUnauthorizedUser_thenReturnForbidden() throws Exception {
        // Simulate forbidden access
        when(taskService.markTaskAsCompleted(eq(1L), anyString()))
                .thenThrow(new AccessDeniedException("User not allowed to modify this task"));

        // When & Then
        mockMvc.perform(patch("/api/v1/tasks/1/complete")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser")
    void whenGetOverdueTasks_thenReturnList() throws Exception {
        // Given
        TaskResponse overdueTask = TaskResponse.builder()
                .id(1L)
                .title("Overdue Task")
                .status(Task.TaskStatus.PENDING)
                .build();

        when(taskService.getOverdueTasks(eq("testuser"), any(LocalDateTime.class)))
                .thenReturn(List.of(overdueTask));

        // When & Then
        mockMvc.perform(get("/api/v1/tasks/overdue")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void whenGetOverdueTasks_withReferenceTime_thenReturnFilteredTasks() throws Exception {
        // Given
        TaskResponse overdueTask = TaskResponse.builder()
                .id(2L)
                .title("Overdue by Reference Time")
                .status(Task.TaskStatus.PENDING)
                .build();

        when(taskService.getOverdueTasks(eq("testuser"), any(LocalDateTime.class)))
                .thenReturn(List.of(overdueTask));

        // When & Then
        mockMvc.perform(get("/api/v1/tasks/overdue")
                        .param("referenceTime", "2025-10-20T12:00:00")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].title").value("Overdue by Reference Time"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void whenNoOverdueTasks_thenReturnEmptyList() throws Exception {
        // Given
        when(taskService.getOverdueTasks(eq("testuser"), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/tasks/overdue")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void whenUnauthenticated_thenReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/overdue")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    void whenGetOverdueTasks_withInvalidReferenceTime_thenReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/tasks/overdue")
                        .param("referenceTime", "not-a-valid-date")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

}

