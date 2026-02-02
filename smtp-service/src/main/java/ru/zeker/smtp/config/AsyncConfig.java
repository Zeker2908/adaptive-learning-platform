package ru.zeker.smtp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class AsyncConfig {
    @Bean(name = "emailExecutor")
    public Executor emailExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
