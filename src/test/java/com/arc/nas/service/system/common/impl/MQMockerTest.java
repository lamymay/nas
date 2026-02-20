package com.arc.nas.service.system.common.impl;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MQMockerTest {
    private static final Logger log = LoggerFactory.getLogger(MQMockerTest.class);

    private MQMocker mqMocker;
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    void setup() throws UnknownHostException {
        // H2 内存数据库配置
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:nas_full_test;DB_CLOSE_DELAY=-1;MODE=MySQL");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        jdbcTemplate = new JdbcTemplate(dataSource);

        // 初始化表结构
        jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS sys_mq_task (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        topic VARCHAR(100),
                        payload TEXT,
                        status INT DEFAULT 0,
                        version INT DEFAULT 0,
                        retry_count INT DEFAULT 0,
                        max_retry INT DEFAULT 5,
                        worker_id VARCHAR(100),
                        last_error VARCHAR(255),
                        execute_time TIMESTAMP,
                        update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                """);

        mqMocker = new MQMocker(jdbcTemplate);
    }

    @BeforeEach
    void clean() {
        jdbcTemplate.execute("DELETE FROM sys_mq_task");
    }

    @Test
    @DisplayName("场景1：基础流程 - 修复异步删除延迟")
    void testBasicFlow() throws InterruptedException {
        String topic = "BASIC_TEST";
        CountDownLatch latch = new CountDownLatch(1);

        // 注册处理器
        MQMocker.registerConsumer(topic, payload -> {
            log.info("【测试】收到消息: {}", payload);
            latch.countDown();
        });

        mqMocker.send(topic, "hello");
        mqMocker.autoDispatch();

        // 1. 等待业务逻辑执行完
        assertTrue(latch.await(2, TimeUnit.SECONDS), "任务应在规定时间内完成");

        // 2. 【核心修复】给异步删除留出极短的 IO 时间
        // 即使只有 50ms，也足以让子线程完成 DELETE 操作
        Thread.sleep(100);

        // 3. 验证
        Long count = jdbcTemplate.queryForObject("SELECT count(*) FROM sys_mq_task", Long.class);
        assertEquals(0, count, "成功处理的任务必须被删除 (当前仍残留: " + count + ")");
    }
    @Test
    @DisplayName("场景2：高并发抢占 - 确保同一任务不被重复消费")
    void testConcurrentGrab() throws InterruptedException {
        String topic = "CONCURRENT_TEST";
        AtomicInteger executeCount = new AtomicInteger(0);

        // 注册一个耗时 100ms 的处理器，增加冲突概率
        MQMocker.registerConsumer(topic, payload -> {
            executeCount.incrementAndGet();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        });

        mqMocker.send(topic, "single-task");

        // 模拟多个线程同时触发调度
        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                mqMocker.autoDispatch();
                latch.countDown();
            }).start();
        }

        latch.await(5, TimeUnit.SECONDS);
        // 核心断言：虽然 5 个线程都在抢，但由于乐观锁，只有 1 个能抢到
        assertEquals(1, executeCount.get(), "任务被重复执行了！");
    }

    @Test
    @DisplayName("场景3：重试机制 - 模拟失败并验证指数退避")
    void testRetryMechanism() throws InterruptedException {
        String topic = "RETRY_TEST";
        MQMocker.registerConsumer(topic, payload -> {
            throw new RuntimeException("Disk Busy");
        });

        mqMocker.send(topic, "retry-data");
        mqMocker.autoDispatch();

        // 稍微等待异步 handleFailure 完成
        Thread.sleep(500);

        var task = jdbcTemplate.queryForMap("SELECT * FROM sys_mq_task WHERE topic = ?", topic);
        assertEquals(0, task.get("status"), "失败后应回到待处理状态(0)");
        assertEquals(1, task.get("retry_count"), "重试次数应递增");
        assertNotNull(task.get("execute_time"), "应设置下一次执行时间");
        assertTrue(task.get("last_error").toString().contains("Disk Busy"));
    }

    @Test
    @DisplayName("场景4：死信队列 - 达到最大重试次数后标记失败")
    void testDeadLetter() throws Exception {
        String topic = "DEAD_TEST";
        // 模拟重试了 4 次，max_retry 是 5
        jdbcTemplate.execute("INSERT INTO sys_mq_task (topic, payload, status, retry_count, max_retry, version, execute_time) " + "VALUES ('DEAD_TEST', 'bad-data', 0, 4, 5, 0, CURRENT_TIMESTAMP)");

        MQMocker.registerConsumer(topic, payload -> {
            throw new RuntimeException("Fatal Error");
        });

        mqMocker.autoDispatch();

        // --- 核心修复：轮询等待状态变更 ---
        boolean success = false;
        for (int i = 0; i < 20; i++) { // 最多等 2 秒 (20 * 100ms)
            Integer status = jdbcTemplate.queryForObject("SELECT status FROM sys_mq_task WHERE topic = 'DEAD_TEST'", Integer.class);
            if (status != null && status == 3) {
                success = true;
                break;
            }
            Thread.sleep(100);
        }

        assertTrue(success, "任务在超时时间内未进入死信状态(3)");
    }

    @Test
    @DisplayName("场景5：启动自愈 - 恢复重启前卡在 status=1 的任务")
    void testStartupRecovery() {
        // 模拟重启前：任务已被某 worker 领走 (status=1)
        jdbcTemplate.execute("INSERT INTO sys_mq_task (topic, payload, status, worker_id, version, execute_time) " + "VALUES ('RECOVERY_TEST', 'data', 1, 'old-worker', 1, CURRENT_TIMESTAMP)");

        // 触发自愈逻辑（模拟 Spring 启动事件）
        mqMocker.autoRecoverOnStartup();

        Integer status = jdbcTemplate.queryForObject("SELECT status FROM sys_mq_task WHERE topic = 'RECOVERY_TEST'", Integer.class);
        Object workerId = jdbcTemplate.queryForObject("SELECT worker_id FROM sys_mq_task WHERE topic = 'RECOVERY_TEST'", Object.class);

        assertEquals(0, status, "重启后任务应恢复为待处理(0)");
        assertNull(workerId, "重启后应清除 worker_id");
    }

    @Test
    @DisplayName("场景6：延迟执行 - 判定 execute_time 是否生效")
    void testDeferredTask() throws InterruptedException {
        String topic = "DEFERRED_TOPIC";
        AtomicInteger callCount = new AtomicInteger(0);
        MQMocker.registerConsumer(topic, p -> callCount.incrementAndGet());

        // 插入一条 1 小时后才执行的任务
        jdbcTemplate.execute("INSERT INTO sys_mq_task (topic, payload, status, execute_time) " + "VALUES ('DEFERRED_TOPIC', 'later', 0, DATEADD('HOUR', 1, CURRENT_TIMESTAMP))");

        mqMocker.autoDispatch();
        Thread.sleep(200);

        assertEquals(0, callCount.get(), "延迟任务不应被立即执行");

        // 验证状态依然是待处理
        Integer status = jdbcTemplate.queryForObject("SELECT status FROM sys_mq_task WHERE topic = 'DEFERRED_TOPIC'", Integer.class);
        assertEquals(0, status);
    }

    @Test
    @DisplayName("场景7：大数据量与特殊字符 - 稳定性测试")
    void testLargePayloadWithSpecialChars() throws InterruptedException {
        String topic = "PAYLOAD_TEST";
        String complexPayload = "{\"key\": \"value with 'quotes' and \n newlines and \uD83D\uDE80 emoji\"}";
        CountDownLatch latch = new CountDownLatch(1);

        MQMocker.registerConsumer(topic, p -> {
            if (p.equals(complexPayload)) latch.countDown();
        });

        mqMocker.send(topic, complexPayload);
        mqMocker.autoDispatch();

        assertTrue(latch.await(2, TimeUnit.SECONDS), "特殊字符或大数据量 Payload 处理失败");
    }
    @Test
    @DisplayName("场景8：并发 Topic 处理隔离性")
    void testTopicIsolation() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        MQMocker.registerConsumer("T1", p -> latch.countDown());
        MQMocker.registerConsumer("T2", p -> latch.countDown());

        mqMocker.send("T1", "d1");
        mqMocker.send("T2", "d2");

        // 核心修复：因为一次只取一个，所以要触发两次
        mqMocker.autoDispatch(); // 处理 T1
        mqMocker.autoDispatch(); // 处理 T2

        assertTrue(latch.await(2, TimeUnit.SECONDS), "不同 Topic 的并发处理应相互隔离");
    }
    @Test
    @DisplayName("测试 Topic 注册与自动分发逻辑 - 修复版")
    void testRegisterAndAutoDispatch() throws InterruptedException {
        String topic = "NAS_THUMBNAIL";
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);

        // 1. 注册处理器
        MQMocker.registerConsumer(topic, payload -> {
            log.info("处理任务: {}", payload);
            successCount.incrementAndGet();
            latch.countDown();
        });

        // 2. 发送两条消息
        mqMocker.send(topic, "img1.jpg");
        mqMocker.send(topic, "img2.jpg");

        // 3. 循环触发调度（模拟真实 @Scheduled 的持续运行）
        // 因为一次只处理一个，所以这里必须触发多次
        for (int i = 0; i < 5; i++) { // 给一点余量，触发 5 次
            mqMocker.autoDispatch();
            // 如果任务已经全部领完，可以提前跳出（可选）
            if (latch.getCount() == 0) break;
            Thread.sleep(50); // 给异步线程一点点执行时间
        }

        // 4. 验证结果
        assertTrue(latch.await(5, TimeUnit.SECONDS), "任务处理超时，当前完成数: " + successCount.get());
        assertEquals(2, successCount.get(), "任务总数不匹配");

        Long dbCount = jdbcTemplate.queryForObject("SELECT count(*) FROM sys_mq_task", Long.class);
        assertEquals(0, dbCount, "成功的任务应从数据库中物理删除");
    }
    @Test
    @DisplayName("压力测试：模拟海量任务堆积与高频并发处理")
    void testHighLoadThroughput() throws InterruptedException {
        String topic = "HIGH_LOAD_TOPIC";
        int taskCount = 100; // 任务总量
        CountDownLatch latch = new CountDownLatch(taskCount);
        AtomicInteger processCount = new AtomicInteger(0);

        // 1. 注册一个模拟耗时的处理器 (模拟 NAS 密集计算)
        MQMocker.registerConsumer(topic, payload -> {
            try {
                Thread.sleep(10); // 模拟耗时操作，增加并发冲突的可能性
                processCount.incrementAndGet();
                latch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 2. 瞬间插入海量任务
        for (int i = 0; i < taskCount; i++) {
            mqMocker.send(topic, "payload-" + i);
        }

        // 3. 模拟多个后台线程高频触发调度 (模拟真实 @Scheduled 环境)
        // 既然一次只取一个，我们要确保触发的总次数远大于任务数
        int dispatcherThreads = 10;
        ExecutorService dispatchPool = Executors.newFixedThreadPool(dispatcherThreads);

        // 每个调度线程循环触发，直到任务全部领完
        for (int i = 0; i < dispatcherThreads; i++) {
            dispatchPool.submit(() -> {
                while (latch.getCount() > 0) {
                    mqMocker.autoDispatch();
                    try {
                        // 稍微歇一下，防止数据库连接池瞬间爆炸，模拟真实 100ms 间隔
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }

        // 4. 等待全部处理完成，增加容错时间
        boolean finished = latch.await(20, TimeUnit.SECONDS);

        // 5. 验证结果
        assertTrue(finished, "在压力下任务处理超时，仅完成: " + processCount.get());
        assertEquals(taskCount, processCount.get(), "已处理任务总数应与发送总数严格匹配");

        // 验证数据库最终状态：已成功的任务应全部被物理删除
        Long dbCount = jdbcTemplate.queryForObject("SELECT count(*) FROM sys_mq_task", Long.class);
        assertEquals(0, dbCount, "压力测试后数据库应清空");

        dispatchPool.shutdownNow();
    }
    /**
     * 辅助方法：持续触发调度直到数据库为空或超时
     */
    private void triggerUntilEmpty(int maxRetries) throws InterruptedException {
        for (int i = 0; i < maxRetries; i++) {
            mqMocker.autoDispatch();
            Long count = jdbcTemplate.queryForObject("SELECT count(*) FROM sys_mq_task WHERE status = 0", Long.class);
            if (count == null || count == 0) break;
            Thread.sleep(50); // 给异步处理一点时间
        }
    }
    @Test
    @DisplayName("场景 深度重启测试 - 模拟断电后的任务自愈 - 修复版")
    void testDeepSystemRestart() throws InterruptedException {
        String topic = "RESTART_TOPIC";
        AtomicInteger processCount = new AtomicInteger(0);

        // 1. 重新注册处理器（确保环境干净）
        MQMocker.registerConsumer(topic, p -> {
            log.info("【自愈测试】消费成功: {}", p);
            processCount.incrementAndGet();
        });

        // 2. 模拟崩溃现场
        // 任务 B：原本正在处理 (status=1)
        jdbcTemplate.execute("INSERT INTO sys_mq_task (topic, payload, status, worker_id, execute_time, version) " +
                "VALUES ('RESTART_TOPIC', 'task-B', 1, 'old-worker', DATEADD('MINUTE', -1, CURRENT_TIMESTAMP), 1)");

        // 任务 C：原本就在排队 (status=0)
        jdbcTemplate.execute("INSERT INTO sys_mq_task (topic, payload, status, execute_time, version) " +
                "VALUES ('RESTART_TOPIC', 'task-C', 0, DATEADD('MINUTE', -1, CURRENT_TIMESTAMP), 1)");

        // 3. 执行自愈逻辑：此时任务 B 的 status 会从 1 变回 0
        log.info("--- 模拟系统重新启动 ---");
        mqMocker.autoRecoverOnStartup();

        // 4. 循环触发调度
        // 因为有两个任务需要处理，必须触发多次 autoDispatch
        for (int i = 0; i < 10; i++) {
            mqMocker.autoDispatch();
            if (processCount.get() == 2) break; // 任务全部完成后提前退出
            Thread.sleep(100);
        }

        // 5. 验证结果
        assertEquals(2, processCount.get(), "重启并自愈后，2个任务都应该被重新处理");

        Long dbCount = jdbcTemplate.queryForObject("SELECT count(*) FROM sys_mq_task", Long.class);
        assertEquals(0, dbCount, "处理完的任务应从数据库删除");
    }

    @Test
    @DisplayName("场景 自动清理僵尸任务 - 解决任务执行卡死问题")
    void testCleanupZombieTasks() {
        String topic = "ZOMBIE_TEST";

        // 1. 模拟一个 2 小时前被领走（status=1）的任务
        // 注意：我们将 update_time 设置为 2 小时前
        jdbcTemplate.execute("INSERT INTO sys_mq_task (topic, payload, status, worker_id, update_time, execute_time) " + "VALUES ('ZOMBIE_TEST', 'stuck-data', 1, 'zombie-worker', " + "DATEADD('HOUR', -2, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP)");

        // 2. 调用清理方法，设定 1 小时为超时阈值
        mqMocker.cleanupZombies(java.time.Duration.ofHours(1));

        // 3. 验证任务是否恢复了自由身
        // 修改点：使用具体的查询条件
        Map<String, Object> task = jdbcTemplate.queryForMap("SELECT * FROM sys_mq_task WHERE topic = 'ZOMBIE_TEST' AND payload = 'stuck-data'");

        assertEquals(0, (Integer) task.get("status"), "长时间占用的任务应该被重置为待处理(0)");
        assertNull(task.get("worker_id"), "应清空僵尸 Worker 的 ID");

        // 4. 验证自愈后的任务能否被正常重新处理
        AtomicInteger callCount = new AtomicInteger(0);
        MQMocker.registerConsumer(topic, p -> callCount.incrementAndGet());
        mqMocker.autoDispatch();

        // 轮询等待一下异步处理完成
        for (int i = 0; i < 10 && callCount.get() == 0; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        assertEquals(1, callCount.get(), "清理后的任务应能被重新调度消费");
    }

    @Test
    @DisplayName("场景 幂等性验证 - 模拟业务成功但数据库删除失败")
    void testIdempotencyOnDeleteFailure() {
        String topic = "IDEM_TEST";
        AtomicInteger processCount = new AtomicInteger(0);

        // 1. 注册一个正常的处理器
        MQMocker.registerConsumer(topic, p -> processCount.incrementAndGet());

        // 2. 发送一个任务
        mqMocker.send(topic, "idem-data");

        // 3. 模拟“影子处理”：手动修改数据库让 DELETE 无法精准匹配（比如修改 version）
        // 或者在你的代码逻辑里，如果 delete 失败，下次 dispatch 依然能看到它
        mqMocker.autoDispatch();

        // 假设因为某种原因，任务没被删掉，再次触发 dispatch
        mqMocker.autoDispatch();

        // 验证：即使触发两次，由于状态已经是 1 或已被第一个线程领走，业务逻辑不应重复执行
        // 这取决于你 autoDispatch 里的 SQL 是否严格过滤了 status=0
        assertEquals(1, processCount.get(), "业务逻辑不应被重复执行");
    }
}