package com.arc.nas.timer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulingConfig {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1); // 设置定时任务线程池大小
        scheduler.setThreadNamePrefix("MyScheduled-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true); // 优雅停机
        scheduler.setAwaitTerminationSeconds(60);
        return scheduler;
    }
}