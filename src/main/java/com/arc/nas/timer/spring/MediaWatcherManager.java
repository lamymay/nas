//package com.arc.nas.timer;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.integration.dsl.IntegrationFlow;
//import org.springframework.integration.dsl.context.IntegrationFlowContext;
//import org.springframework.integration.file.FileReadingMessageSource;
//import org.springframework.integration.file.dsl.Files;
//import org.springframework.integration.file.filters.FileSystemPersistentAcceptOnceFileListFilter;
//import org.springframework.integration.jdbc.metadata.JdbcMetadataStore;
//import org.springframework.integration.metadata.ConcurrentMetadataStore;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//import org.springframework.stereotype.Service;
//
//import javax.sql.DataSource;
//import java.io.File;
//import java.util.concurrent.Executor;
//
//@Slf4j
//@Service
//public class MediaWatcherManager {
//
//    private final IntegrationFlowContext flowContext;
//    private final ConcurrentMetadataStore metadataStore;
//    private final MyMediaBusinessService businessService; // 你的业务 Service
//
//    public MediaWatcherManager(IntegrationFlowContext flowContext,
//                               ConcurrentMetadataStore metadataStore,
//                               MyMediaBusinessService businessService) {
//        this.flowContext = flowContext;
//        this.metadataStore = metadataStore;
//        this.businessService = businessService;
//    }
//
//    /**
//     * 注册或更新一个文件夹监控
//     * @param configId 数据库配置ID，作为 Flow 的唯一标识
//     * @param pathStr 文件夹绝对路径
//     */
//    public void upsertWatcher(String configId, String pathStr) {
//        // 1. 如果已存在，先移除旧的 Flow (实现热更新)
//        if (flowContext.getRegistrationById(configId) != null) {
//            flowContext.remove(configId);
//            log.info("停止旧监听器: {}", configId);
//        }
//
//        File directory = new File(pathStr);
//        if (!directory.exists() || !directory.isDirectory()) {
//            log.warn("路径无效，跳过注册: {}", pathStr);
//            return;
//        }
//
//        // 2. 定义基于 H2 的持久化过滤器（去重核心）
//        // "media_scan_" 是前缀，用于在 H2 中区分不同的监控任务
//        FileSystemPersistentAcceptOnceFileListFilter filter =
//                new FileSystemPersistentAcceptOnceFileListFilter(metadataStore, "media_scan_" + configId);
//        filter.setFlushOnUpdate(true);
//
//        // 3. 构建动态流
//        IntegrationFlow flow = IntegrationFlow
//                .from(Files.inboundAdapter(directory)
//                                .recursive(true)      // 递归子目录
//                                .useWatchService(true) // 开启 NIO 实时监控
//                                .filter(filter),       // 挂载 H2 过滤器
//                        e -> e.poller(p -> p.fixedDelay(1000).maxMessagesPerPoll(100))) // 每秒检查一次队列
//                .channel("fileProcessingChannel") // 发送到异步通道
//                .get();
//
//        // 4. 注册并启动
//        flowContext.registration(flow).id(configId).register();
//        log.info("成功启动路径监控: {}", pathStr);
//    }
//
//    public void stopWatcher(String configId) {
//        flowContext.remove(configId);
//    }
//}