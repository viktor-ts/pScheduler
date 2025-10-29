package com.masa.pScheduler.service;

import com.masa.pScheduler.dto.TaskCreateRequest;
import com.masa.pScheduler.dto.TaskResponse;
import com.masa.pScheduler.exception.ResourceNotFoundException;
import com.masa.pScheduler.model.Task;
import com.masa.pScheduler.model.User;
import com.masa.pScheduler.repository.TaskRepository;
import com.masa.pScheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public TaskResponse createTask(TaskCreateRequest request, String username) {
        log.info("Creating task for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .priority(request.getPriority() != null ? request.getPriority() : Task.Priority.MEDIUM)
                .status(Task.TaskStatus.PENDING)
                .isRecurring(request.isRecurring())
                .recurrencePattern(request.getRecurrencePattern())
                .tags(request.getTags())
                .user(user)
                .build();
        
        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully with ID: {}", savedTask.getId());
        
        return mapToResponse(savedTask);
    }
    
    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasksForUser(String username) {
        log.info("Fetching all tasks for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return taskRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId, String username) {
        log.info("Fetching task with ID: {} for user: {}", taskId, username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Task task = taskRepository.findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        
        return mapToResponse(task);
    }
    
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByStatus(Task.TaskStatus status, String username) {
        log.info("Fetching tasks with status: {} for user: {}", status, username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return taskRepository.findByUserIdAndStatus(user.getId(), status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteTask(Long taskId, String username) {
        log.info("Deleting task with ID: {} for user: {}", taskId, username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Task task = taskRepository.findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        
        taskRepository.delete(task);
        log.info("Task deleted successfully: {}", taskId);
    }
    
    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .deadline(task.getDeadline())
                .completedAt(task.getCompletedAt())
                .isRecurring(task.isRecurring())
                .recurrencePattern(task.getRecurrencePattern())
                .tags(task.getTags())
                .userId(task.getUser().getId())
                .username(task.getUser().getUsername())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .isOverdue(task.isOverdue())
                .build();
    }
}