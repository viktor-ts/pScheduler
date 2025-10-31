package com.masa.pScheduler.service;

import com.masa.pScheduler.dto.TaskCreateRequest;
import com.masa.pScheduler.dto.TaskResponse;
import com.masa.pScheduler.dto.TaskUpdateRequest;
import com.masa.pScheduler.events.TaskCompletedEvent;
import com.masa.pScheduler.exception.ResourceNotFoundException;
import com.masa.pScheduler.model.Task;
import com.masa.pScheduler.model.User;
import com.masa.pScheduler.repository.TaskRepository;
import com.masa.pScheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;
    
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

    @Transactional
    public TaskResponse updateTask(Long taskId, TaskUpdateRequest request, String username) {
        log.info("Updating task with ID: {} for user: {}", taskId, username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Task task = taskRepository.findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getDeadline() != null) {
            task.setDeadline(request.getDeadline());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
            if (request.getStatus() == Task.TaskStatus.COMPLETED) {
                task.setCompletedAt(LocalDateTime.now());
            }
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getTags() != null) {
            task.setTags(request.getTags());
        }

        Task updatedTask = taskRepository.save(task);
        log.info("Task updated successfully: {}", updatedTask.getId());

        return mapToResponse(updatedTask);
    }

    @Transactional
    public TaskResponse markTaskAsCompleted(Long taskId, String username) {
        log.info("Marking task as completed: {} for user: {}", taskId, username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Task task = taskRepository.findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (task.getStatus() == Task.TaskStatus.COMPLETED) {
            log.info("Task {} is already completed. Returning current state.", taskId);
            return mapToResponse(task);
        }

        task.markAsCompleted();
        Task completedTask = taskRepository.save(task);

        log.info("Task marked as completed: {}", completedTask.getId());

        eventPublisher.publishEvent(new TaskCompletedEvent(this, List.of(completedTask), username));

        return mapToResponse(completedTask);
    }

    @Transactional
    public List<TaskResponse> markTasksAsCompleted(List<Long> taskIds, String username) {
        log.info("Bulk completing {} tasks for user: {}", taskIds.size(), username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Task> tasks = taskRepository.findAllByIdInAndUserId(taskIds, user.getId());

        if (tasks.size() != taskIds.size()) {
            log.warn("Mismatch: Found {} tasks but {} IDs were provided", tasks.size(), taskIds.size());
            throw new ResourceNotFoundException("One or more tasks not found for this user");
        }

        tasks.forEach(task -> {
            if (task.getStatus() != Task.TaskStatus.COMPLETED) {
                task.markAsCompleted();
            }
        });

        List<Task> updatedTasks = taskRepository.saveAll(tasks);

        log.info("Successfully completed {} tasks for user: {}", updatedTasks.size(), username);

        eventPublisher.publishEvent(new TaskCompletedEvent(this, updatedTasks, username));

        return updatedTasks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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

    @Transactional(readOnly = true)
    public List<TaskResponse> getOverdueTasks(String username, LocalDateTime referenceTime) {
        log.info("Fetching overdue tasks for user: {} as of {}", username, referenceTime);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LocalDateTime effectiveTime = referenceTime != null ? referenceTime : LocalDateTime.now();

        return taskRepository.findOverdueTasks(user.getId(), effectiveTime)
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