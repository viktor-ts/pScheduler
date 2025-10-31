package com.masa.pScheduler.events;

import com.masa.pScheduler.model.Task;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class TaskCompletedEvent extends ApplicationEvent {
    private final List<Task> completedTasks;
    private final String username;

    public TaskCompletedEvent(Object source, List<Task> completedTasks, String username) {
        super(source);
        this.completedTasks = completedTasks;
        this.username = username;
    }
}
