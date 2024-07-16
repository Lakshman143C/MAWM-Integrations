package com.floordecor.inbound.config;

import com.supplychain.foundation.config.ThreadContextTaskDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ThreadPoolExecutor;
@Configuration
public class AsyncConfig {
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("masterJob::");
        scheduler.setPoolSize(100);
        return scheduler;
    }
    /** using for Spring Batch Steps */
    @Bean(name = "jobPoolTaskExecutor")
    public TaskExecutor jobPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("Async-Job-Task::");
        executor.setCorePoolSize(75);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(75);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setTaskDecorator(new ThreadContextTaskDecorator());
        executor.initialize();
        return executor;
    }

    /** using for Spring Batch jobs */
    @Bean(name = "jobTaskExecutor")
    public TaskExecutor jobTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("File-Job::");
        executor.setCorePoolSize(75);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(75);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setTaskDecorator(new ThreadContextTaskDecorator());
        executor.initialize();
        return executor;
    }

    /** using for Spring Integration file polling */
    @Bean(name = "filePollingTaskExecutor")
    public TaskExecutor filePollingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("File-Poll::");
        executor.setCorePoolSize(75);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(75);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setTaskDecorator(new ThreadContextTaskDecorator());
        executor.initialize();
        return executor;
    }

    @Bean(name = "ruleProcessTaskExecutor")
    public TaskExecutor ruleProcessTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("Item-Rule::");
        executor.setCorePoolSize(75);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(75);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setTaskDecorator(new ThreadContextTaskDecorator());
        executor.initialize();
        return executor;
    }
}
