package com.cumulocity.opcua.gateway.service.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ErrorHandler;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
public class ServiceConfiguration {
    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(2);
        executor.setThreadNamePrefix("MyExecutor-");
        executor.initialize();
        return executor;
    }

    @Bean
    public ThreadPoolTaskScheduler taskExecutor() {
        final ThreadPoolTaskScheduler result = new ThreadPoolTaskScheduler();
        result.setPoolSize(2);
        result.setThreadNamePrefix("MyTaskExecutor-");
        result.setErrorHandler(new ErrorHandler() {
            @Override
            public void handleError(Throwable throwable) {
                log.error(throwable.getMessage(), throwable);
            }
        });
        return result;
    }
}
