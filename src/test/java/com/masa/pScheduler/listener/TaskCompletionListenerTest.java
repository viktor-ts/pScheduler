package com.masa.pScheduler.listener;

import com.masa.pScheduler.events.TaskCompletedEvent;
import com.masa.pScheduler.listeners.TaskCompletionListener;
import com.masa.pScheduler.model.Task;
import com.masa.pScheduler.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class TaskCompletionListenerTest {

    @InjectMocks
    private TaskCompletionListener listener;

    @Captor
    private ArgumentCaptor<TaskCompletedEvent> eventCaptor;

    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        testTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .status(Task.TaskStatus.COMPLETED)
                .user(testUser)
                .build();
    }

    @Test
    void whenTaskCompletedEventReceived_thenLogHandled() {
        // Given
        TaskCompletedEvent event = new TaskCompletedEvent(this, List.of(testTask), "testuser");

        // When
        assertDoesNotThrow(() -> listener.handleTaskCompletion(event));
    }

    @Test
    void whenMultipleTasksCompleted_thenListenerHandlesAll() {
        // Given
        Task task1 = Task.builder().id(1L).title("Task 1").status(Task.TaskStatus.COMPLETED).user(testUser).build();
        Task task2 = Task.builder().id(2L).title("Task 2").status(Task.TaskStatus.COMPLETED).user(testUser).build();

        TaskCompletedEvent event = new TaskCompletedEvent(this, List.of(task1, task2), "testuser");

        // When / Then
        assertDoesNotThrow(() -> listener.handleTaskCompletion(event));
    }
}
