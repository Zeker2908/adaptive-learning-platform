package ru.zeker.smtp.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {
    @Bean
    public Executor emailExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    public AsyncTaskExecutor virtualThreadTaskExecutor(@Qualifier("emailExecutor") Executor executor) {
        return new TaskExecutorAdapter(executor);
    }
}
