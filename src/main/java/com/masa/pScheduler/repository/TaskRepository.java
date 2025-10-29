package com.masa.pScheduler.repository;

import com.masa.pScheduler.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findByUserId(Long userId);
    
    List<Task> findByUserIdAndStatus(Long userId, Task.TaskStatus status);
    
    List<Task> findByUserIdOrderByDeadlineAsc(Long userId);
    
    List<Task> findByUserIdAndPriority(Long userId, Task.Priority priority);
    
    Optional<Task> findByIdAndUserId(Long id, Long userId);
    
    @Query("SELECT t FROM Task t WHERE t.user.id = :userId " +
           "AND t.deadline BETWEEN :start AND :end " +
           "ORDER BY t.deadline ASC")
    List<Task> findTasksByUserAndDateRange(
        @Param("userId") Long userId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.user.id = :userId AND t.status = :status")
    long countByUserIdAndStatus(
        @Param("userId") Long userId,
        @Param("status") Task.TaskStatus status
    );
}