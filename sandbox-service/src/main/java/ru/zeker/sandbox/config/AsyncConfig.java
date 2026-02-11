package ru.zeker.sandbox.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import ru.zeker.common.dto.kafka.solution.SolutionExecRequest;

import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public AsyncTaskExecutor virtualThreadTaskExecutor() {
        return new TaskExecutorAdapter(
                Executors.newVirtualThreadPerTaskExecutor()
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SolutionExecRequest>
    solutionExecKafkaListenerContainerFactory(
            ConsumerFactory<String, SolutionExecRequest> consumerFactory,
            @Qualifier("virtualThreadTaskExecutor") AsyncTaskExecutor executorService
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, SolutionExecRequest>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(4);

        factory.getContainerProperties().setListenerTaskExecutor(executorService);

        return factory;
    }
}
