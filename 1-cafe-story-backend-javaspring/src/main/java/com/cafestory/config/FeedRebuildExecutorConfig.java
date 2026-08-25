package com.cafestory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class FeedRebuildExecutorConfig {

    public static final String FEED_REBUILD_EXECUTOR = "feedRecommendationRebuildExecutor";

    @Bean(name = FEED_REBUILD_EXECUTOR)
    public TaskExecutor feedRecommendationRebuildExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("feed-rebuild-");
        executor.initialize();
        return executor;
    }
}
