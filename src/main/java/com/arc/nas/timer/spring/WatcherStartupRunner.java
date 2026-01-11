//package com.arc.nas.timer.spring;
//
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Component;
//
//@Component
//public class WatcherStartupRunner {
//
//    private final MediaWatcherManager watcherManager;
//    private final MyLibraryRepository repository; // 假设这是你的媒体库路径表
//
//    public WatcherStartupRunner(MediaWatcherManager watcherManager, MyLibraryRepository repository) {
//        this.watcherManager = watcherManager;
//        this.repository = repository;
//    }
//
//    // 当 Spring Boot 启动完成并准备就绪时触发
//    @EventListener(ApplicationReadyEvent.class)
//    public void initWatchers() {
//        // 从 H2 数据库中捞出所有状态为“激活”的监控路径
//        repository.findAllActivePaths().forEach(config -> {
//            watcherManager.upsertWatcher(
//                config.getId().toString(),
//                config.getPath()
//            );
//        });
//    }
//}