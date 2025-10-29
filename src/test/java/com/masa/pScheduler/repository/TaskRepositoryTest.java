package com.masa.pScheduler.repository;

import com.masa.pScheduler.config.JpaConfig;
import com.masa.pScheduler.model.Task;
import com.masa.pScheduler.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class TaskRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .isActive(true)
                .build();
        testUser = entityManager.persist(testUser);
        entityManager.flush();
    }
    
    @Test
    void whenFindByUserId_thenReturnTasks() {
        // Given
        Task task1 = Task.builder()
                .title("Task 1")
                .description("Description 1")
                .deadline(LocalDateTime.now().plusDays(1))
                .user(testUser)
                .build();
        
        Task task2 = Task.builder()
                .title("Task 2")
                .description("Description 2")
                .deadline(LocalDateTime.now().plusDays(2))
                .user(testUser)
                .build();
        
        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.flush();
        
        // When
        List<Task> found = taskRepository.findByUserId(testUser.getId());
        
        // Then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Task::getTitle).containsExactlyInAnyOrder("Task 1", "Task 2");
    }
    
    @Test
    void whenFindByUserIdAndStatus_thenReturnFilteredTasks() {
        // Given
        Task pendingTask = Task.builder()
                .title("Pending Task")
                .status(Task.TaskStatus.PENDING)
                .deadline(LocalDateTime.now().plusDays(1))
                .user(testUser)
                .build();
        
        Task completedTask = Task.builder()
                .title("Completed Task")
                .status(Task.TaskStatus.COMPLETED)
                .deadline(LocalDateTime.now().plusDays(1))
                .user(testUser)
                .build();
        
        entityManager.persist(pendingTask);
        entityManager.persist(completedTask);
        entityManager.flush();
        
        // When
        List<Task> pendingTasks = taskRepository.findByUserIdAndStatus(
                testUser.getId(), 
                Task.TaskStatus.PENDING
        );
        
        // Then
        assertThat(pendingTasks).hasSize(1);
        assertThat(pendingTasks.get(0).getTitle()).isEqualTo("Pending Task");
    }
    
    @Test
    void whenFindByIdAndUserId_thenReturnTask() {
        // Given
        Task task = Task.builder()
                .title("Test Task")
                .deadline(LocalDateTime.now().plusDays(1))
                .user(testUser)
                .build();
        task = entityManager.persist(task);
        entityManager.flush();
        
        // When
        Optional<Task> found = taskRepository.findByIdAndUserId(task.getId(), testUser.getId());
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Task");
    }
}

