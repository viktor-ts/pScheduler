package com.masa.pScheduler.listeners;

import com.masa.pScheduler.events.TaskCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskCompletionListener {

    @Async
    @EventListener
    public void handleTaskCompletion(TaskCompletedEvent event) {
        log.info("📊 Task completion event received for user: {}", event.getUsername());
        log.info("✅ {} task(s) completed: {}", event.getCompletedTasks().size(),
                 event.getCompletedTasks().stream().map(t -> t.getTitle()).toList());

        // TODO: Future integrations (Analytics, Webhooks, Notifications)
    }
}
