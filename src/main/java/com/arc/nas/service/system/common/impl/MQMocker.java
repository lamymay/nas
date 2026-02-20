package com.arc.nas.service.system.common.impl;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Component
@DependsOn("dataSource1")
public class MQMocker {

    private static final Logger log = LoggerFactory.getLogger(MQMocker.class);

    // 状态定义
    private volatile State currentState = State.RUNNING;
    private final AtomicInteger idleCount = new AtomicInteger(0);

    public enum State {RUNNING, PAUSED, SHUTTING_DOWN, STOPPED}

    private static String workerId;
    private final ExecutorService executor = new ThreadPoolExecutor(
            2, 4, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            new ThreadPoolExecutor.CallerRunsPolicy() // 满负荷时，调度线程亲自下场干活，天然限流
    );
    private final JdbcTemplate jdbcTemplate;
    private static final Map<String, Consumer<String>> consumerMap = new ConcurrentHashMap<>();

    public MQMocker(JdbcTemplate jdbcTemplate) throws UnknownHostException {
        workerId = InetAddress.getLocalHost().getHostAddress() + "-" + ThreadLocalRandom.current().nextInt(1000);
        this.jdbcTemplate = jdbcTemplate;
    }

    // ================= 状态控制 API =================

    public State getCurrentState() {
        return currentState;
    }

    /**
     * 暂停：停止分发新任务，但保留线程池
     */
    public void pause() {
        if (this.currentState == State.RUNNING) {
            this.currentState = State.PAUSED;
            log.warn("MQMocker 已【暂停】。");
        }
    }

    /**
     * 恢复：重新开始分发任务
     */
    public void resume() {
        if (this.currentState == State.PAUSED) {
            this.currentState = State.RUNNING;
            log.info("MQMocker 已【恢复】运行。");
        }
    }

    /**
     * 重启：回收当前资源并重置状态
     */
    public void restart() {
        log.info("MQMocker 正在【重启】...");
        stopLogic(); // 执行停止逻辑
        this.currentState = State.RUNNING; // 重置为运行态
        log.info("MQMocker 重启成功。");
    }

    /**
     * 停止：由 Spring 销毁或手动触发
     */
    @PreDestroy
    public void shutdown() {
        if (this.currentState == State.STOPPED) return;
        stopLogic();
    }

    /**
     * 核心停止逻辑（复用）
     */
    private void stopLogic() {
        this.currentState = State.SHUTTING_DOWN;
        log.info("MQMocker 正在停止，回收资源中...");

        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // 增加异常捕获，防止数据库先关闭导致回收报错
        try {
            int rows = jdbcTemplate.update(
                    "UPDATE sys_mq_task SET status = 0, worker_id = NULL WHERE worker_id = ? AND status = 1",
                    workerId
            );
            log.info("MQMocker 已停止，成功回收任务数: {}", rows);
        } catch (Exception e) {
            log.warn("MQMocker 停机回收任务失败（数据库可能已提前关闭）: {}", e.getMessage());
        }

        this.currentState = State.STOPPED;
    }

    // ================= 核心调度逻辑 =================

    private static final int BASE_IDLE_MS = 500;
    // 最大休眠上限 (毫秒)，例如 10 秒扫一次
    private static final int MAX_IDLE_MS = 10000;
    // 记录下一次允许执行的时间戳
    private volatile long nextExecutionTime = 0;

    @Scheduled(fixedDelay = 500) // 基础检查频率改为 0.5s
    public void autoDispatch() {
        if (this.currentState != State.RUNNING) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now < nextExecutionTime) {
            return; // 还没到下次巡逻时间，直接回家睡觉
        }

        String sql = "SELECT * FROM sys_mq_task WHERE status = 0 AND execute_time <= CURRENT_TIMESTAMP " +
                "ORDER BY execute_time ASC, id ASC LIMIT 1";

        List<Map<String, Object>> tasks = jdbcTemplate.queryForList(sql);

        if (!tasks.isEmpty()) {
            // 【命中】有活干：计数器清零，下一次巡逻立即开始
            idleCount.set(0);
            nextExecutionTime = 0;

            Map<String, Object> task = tasks.get(0);
            Consumer<String> processor = consumerMap.get((String) task.get("topic"));
            if (processor != null) {
                grabAndProcess(task, processor);
            }
        } else {
            // 【未命中】没活干：快速增加休眠时间
            int count = idleCount.incrementAndGet();

            // 计算下一次执行的推迟时间：500ms * (2的N次方)
            // 1次空转 = 推迟 0.5s
            // 2次空转 = 推迟 1s
            // 3次空转 = 推迟 2s
            // 4次空转 = 推迟 4s
            // 5次及以上 = 封顶 10s
            long delay = (long) BASE_IDLE_MS * (1L << Math.min(count - 1, 4));
            delay = Math.min(delay, MAX_IDLE_MS);

            nextExecutionTime = now + delay;

            // 只有在空转达到一定次数后才打一条 Trace 日志，避免刷屏
            if (count == 5) {
                log.debug("MQ 进入深度休眠模式，当前扫描间隔: {}ms", delay);
            }
        }
    }

    private boolean grabAndProcess(Map<String, Object> task, Consumer<String> processor) {
        Long id = ((Number) task.get("id")).longValue();
        Integer version = (Integer) task.get("version");
        String payload = (String) task.get("payload");

        String updateSql = "UPDATE sys_mq_task SET status = 1, worker_id = ?, version = version + 1, update_time = CURRENT_TIMESTAMP " +
                "WHERE id = ? AND version = ? AND status = 0";

        int rows = jdbcTemplate.update(updateSql, workerId, id, version);

        if (rows > 0) {
            CompletableFuture.runAsync(() -> {
                try {
                    processor.accept(payload);
                    jdbcTemplate.update("DELETE FROM sys_mq_task WHERE id = ?", id);
                } catch (Exception e) {
                    handleFailureById(id, e.getMessage());
                }
            }, executor);
            return true;
        }
        return false;
    }

    // ================= 工具方法 =================

    public static void registerConsumer(String topic, Consumer<String> processor) {
        consumerMap.put(topic, processor);
        log.info("Topic [{}] 注册成功", topic);
    }

    public void send(String topic, String payload) {
        String sql = "INSERT INTO sys_mq_task (topic, payload, status, retry_count, max_retry, version, execute_time, create_time) " +
                "VALUES (?, ?, 0, 0, 5, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        jdbcTemplate.update(sql, topic, payload);
        // 关键：发送后立刻重置计数器，让下一次 autoDispatch 瞬间通过拦截
        this.idleCount.set(0);
        this.nextExecutionTime = 0;
    }

    private void handleFailureById(Long id, String errorMsg) {
        try {
            MqTask task = jdbcTemplate.queryForObject("SELECT * FROM sys_mq_task WHERE id = ?",
                    new BeanPropertyRowMapper<>(MqTask.class), id);
            if (task != null) handleFailure(task, errorMsg);
        } catch (Exception e) {
            log.error("查询失败任务失败 [ID: {}]", id, e);
        }
    }

    private void handleFailure(MqTask task, String errorMsg) {
        String safeError = errorMsg != null && errorMsg.length() > 200 ? errorMsg.substring(0, 200) : errorMsg;
        if (task.getRetryCount() + 1 >= task.getMaxRetry()) {
            jdbcTemplate.update("UPDATE sys_mq_task SET status = 3, last_error = ?, update_time = CURRENT_TIMESTAMP WHERE id = ?",
                    "MAX_RETRY: " + safeError, task.getId());
        } else {
            String failSql = "UPDATE sys_mq_task SET status = 0, worker_id = NULL, retry_count = retry_count + 1, " +
                    "last_error = ?, update_time = CURRENT_TIMESTAMP, " +
                    "execute_time = DATEADD('SECOND', (retry_count + 1) * 30, CURRENT_TIMESTAMP) WHERE id = ?";
            jdbcTemplate.update(failSql, safeError, task.getId());
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void autoRecoverOnStartup() {
        log.info("NAS 启动：自愈中断任务...");
        jdbcTemplate.update("UPDATE sys_mq_task SET status = 0, worker_id = NULL WHERE status = 1");
    }

    /**
     * 清理僵尸任务：将长时间处于"处理中(1)"状态的任务重置为"待处理(0)"
     * 即使系统没重启，也能自动挽救那些因为意外卡死的任务
     */
    @Scheduled(cron = "0 0 * * * ?") // 建议每小时执行一次，清理“过期”任务
    public void autoCleanup() {
        // 判定 30 分钟没有更新的任务为僵尸任务
        cleanupZombies(java.time.Duration.ofMinutes(30));
    }

    public void cleanupZombies(java.time.Duration timeout) {
        // 只有在运行中或暂停时才清理，避免干扰停止过程
        if (this.currentState == State.SHUTTING_DOWN || this.currentState == State.STOPPED) {
            return;
        }

        java.time.LocalDateTime threshold = java.time.LocalDateTime.now().minus(timeout);

        // 查找那些状态为 1（处理中）且 update_time 已经很久没变动的任务
        String sql = "UPDATE sys_mq_task SET status = 0, worker_id = NULL, update_time = CURRENT_TIMESTAMP " +
                "WHERE status = 1 AND update_time < ?";

        int rows = jdbcTemplate.update(sql, java.sql.Timestamp.valueOf(threshold));

        if (rows > 0) {
            log.warn("【守护进程】检测并清理了 {} 个卡死的僵尸任务（超过 {} 分钟未响应）", rows, timeout.toMinutes());
        }
    }

    /**
     * 强制触发特定 Topic 的单条消费（用于测试接口）
     * 它会无视定时器，立即尝试抢占并执行一条存量任务
     */
    public void forceDispatch(String topic, Consumer<String> processor) {
        // 1. 只捞取该 Topic 下最早的一条待处理任务
        String sql = "SELECT * FROM sys_mq_task " +
                "WHERE topic = ? AND status = 0 AND execute_time <= CURRENT_TIMESTAMP " +
                "ORDER BY execute_time ASC, id ASC LIMIT 1";

        List<Map<String, Object>> tasks = jdbcTemplate.queryForList(sql, topic);

        if (!tasks.isEmpty()) {
            log.info("【手动触发】找到 Topic [{}] 的待处理任务，准备执行...", topic);
            // 2. 直接复用现有的抢占与异步执行逻辑
            grabAndProcess(tasks.get(0), processor);
        } else {
            log.warn("【手动触发】失败：Topic [{}] 当前没有符合条件的待处理任务", topic);
        }
    }
}