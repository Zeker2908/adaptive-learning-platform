package ru.zeker.task.domain.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import ru.zeker.common.dto.task.request.TaskRequest;
import ru.zeker.task.service.TaskService;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DemoDataInitializer {

    private final TaskService taskService;
    private final ObjectMapper objectMapper;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initDemoTasks() {
        if (taskService.hasAnyTasks()) {
            log.info("Tasks already exist — skipping demo data initialization");
            return;
        }

        try {
            InputStream inputStream = new ClassPathResource("data/demo-tasks.json").getInputStream();
            List<TaskRequest> tasks = objectMapper.readValue(inputStream, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, TaskRequest.class));

            log.info("Initializing {} demo tasks...", tasks.size());
            for (TaskRequest task : tasks) {
                taskService.createTask(task);
            }
            log.info("Demo tasks initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize demo tasks", e);
        }
    }
}