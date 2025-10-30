package com.masa.pScheduler.service;

import com.masa.pScheduler.dto.TaskCreateRequest;
import com.masa.pScheduler.dto.TaskResponse;
import com.masa.pScheduler.dto.TaskUpdateRequest;
import com.masa.pScheduler.exception.ResourceNotFoundException;
import com.masa.pScheduler.model.Task;
import com.masa.pScheduler.model.User;
import com.masa.pScheduler.repository.TaskRepository;
import com.masa.pScheduler.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private TaskService taskService;
    
    private User testUser;
    private Task testTask;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();
        
        testTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .deadline(LocalDateTime.now().plusDays(1))
                .status(Task.TaskStatus.PENDING)
                .priority(Task.Priority.MEDIUM)
                .user(testUser)
                .build();
    }
    
    @Test
    void whenCreateTask_thenSuccess() {
        // Given
        TaskCreateRequest request = TaskCreateRequest.builder()
                .title("New Task")
                .description("New Description")
                .deadline(LocalDateTime.now().plusDays(1))
                .priority(Task.Priority.HIGH)
                .build();
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        
        // When
        TaskResponse response = taskService.createTask(request, "testuser");
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo(testTask.getTitle());
        verify(taskRepository, times(1)).save(any(Task.class));
    }
    
    @Test
    void whenCreateTaskWithInvalidUser_thenThrowException() {
        // Given
        TaskCreateRequest request = TaskCreateRequest.builder()
                .title("New Task")
                .deadline(LocalDateTime.now().plusDays(1))
                .build();
        
        when(userRepository.findByUsername("invaliduser")).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> taskService.createTask(request, "invaliduser"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }
    
    @Test
    void whenGetAllTasksForUser_thenReturnTasks() {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findByUserId(1L)).thenReturn(tasks);
        
        // When
        List<TaskResponse> responses = taskService.getAllTasksForUser("testuser");
        
        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitle()).isEqualTo("Test Task");
    }
    

    @Test
    void whenDeleteTask_thenSuccess() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testTask));
        
        // When
        taskService.deleteTask(1L, "testuser");
        
        // Then
        verify(taskRepository, times(1)).delete(testTask);
    }
    
    @Test
    void whenGetTaskById_thenReturnTask() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testTask));
        
        // When
        TaskResponse response = taskService.getTaskById(1L, "testuser");
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Task");
    }

    @Test
    void whenUpdateTask_thenSuccess() {
        // Given
        TaskUpdateRequest updateRequest = TaskUpdateRequest.builder()
                .title("Updated Title")
                .description("Updated Description")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        TaskResponse response = taskService.updateTask(1L, updateRequest, "testuser");

        // Then
        verify(taskRepository, times(1)).save(any(Task.class));
        assertThat(testTask.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    void whenStatusUpdatedToCompleted_thenSetCompletedAt() {
        TaskUpdateRequest request = TaskUpdateRequest.builder()
                .status(Task.TaskStatus.COMPLETED)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.updateTask(1L, request, "testuser");

        assertThat(testTask.getStatus()).isEqualTo(Task.TaskStatus.COMPLETED);
        assertThat(testTask.getCompletedAt()).isNotNull();
        verify(taskRepository, times(1)).save(any(Task.class));
    }


    @Test
    void whenTaskNotFoundForUser_thenThrowResourceNotFound() {
        TaskUpdateRequest updateRequest = TaskUpdateRequest.builder()
                .title("Updated Title")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                taskService.updateTask(1L, updateRequest, "testuser")
        );
    }

    @Test
    void whenMarkTaskAsCompleted_thenTaskCompleted() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        TaskResponse response = taskService.markTaskAsCompleted(1L, "testuser");

        // Then
        verify(taskRepository, times(1)).save(any(Task.class));
        assertThat(testTask.getStatus()).isEqualTo(Task.TaskStatus.COMPLETED);
    }

    @Test
    void whenTaskAlreadyCompleted_thenReturnCurrentState() {
        // Given
        testTask.setStatus(Task.TaskStatus.COMPLETED);
        testTask.setCompletedAt(LocalDateTime.now());

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testTask));

        // When
        TaskResponse response = taskService.markTaskAsCompleted(1L, "testuser");

        // Then
        verify(taskRepository, never()).save(any(Task.class)); // no save since already completed
        assertThat(response.getStatus()).isEqualTo(Task.TaskStatus.COMPLETED);
        assertThat(response.getCompletedAt()).isNotNull();
    }

    @Test
    void whenMarkTaskAsCompleted_thenCompletedAtIsSet() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setCompletedAt(LocalDateTime.now());
            return savedTask;
        });

        // When
        TaskResponse response = taskService.markTaskAsCompleted(1L, "testuser");

        // Then
        assertThat(response.getStatus()).isEqualTo(Task.TaskStatus.COMPLETED);
        assertThat(response.getCompletedAt()).isNotNull();
        assertThat(response.getCompletedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }


    @Test
    void whenUserNotFound_thenThrowException() {
        // Given
        when(userRepository.findByUsername("unknownUser")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> taskService.markTaskAsCompleted(1L, "unknownUser"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void whenTaskNotFound_thenThrowException() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> taskService.markTaskAsCompleted(1L, "testuser"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    void whenGetOverdueTasks_thenReturnOverdueTasks() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findOverdueTasks(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(testTask));
        List<TaskResponse> result = taskService.getOverdueTasks("testuser", null);
        assertThat(result).hasSize(1);
        verify(taskRepository, times(1)).findOverdueTasks(eq(1L), any(LocalDateTime.class));
    }

    @Test
    void whenGetOverdueTasksWithReferenceTime_thenUseProvidedTime() {
        LocalDateTime referenceTime = LocalDateTime.of(2025, 10, 20, 12, 0);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findOverdueTasks(eq(1L), eq(referenceTime)))
                .thenReturn(List.of(testTask));
        List<TaskResponse> result = taskService.getOverdueTasks("testuser", referenceTime);
        verify(taskRepository, times(1)).findOverdueTasks(eq(1L), eq(referenceTime));
        assertThat(result).hasSize(1);
    }

    @Test
    void whenNoOverdueTasks_thenReturnEmptyList() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findOverdueTasks(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        List<TaskResponse> result = taskService.getOverdueTasks("testuser", null);
        assertThat(result).isEmpty();
    }

    @Test
    void whenUserNotFound_thenThrowResourceNotFoundException() {
        when(userRepository.findByUsername("missingUser")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taskService.getOverdueTasks("missingUser", null));
    }


}


