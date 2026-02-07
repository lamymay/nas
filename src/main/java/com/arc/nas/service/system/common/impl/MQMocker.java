package com.arc.nas.service.system.common.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@Component
public class MQMocker {

    private static final Logger log = LoggerFactory.getLogger(MQMocker.class);
    private final String workerId;
    private final ExecutorService executor = Executors.newFixedThreadPool(2); // 根据负载调整
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public MQMocker() throws UnknownHostException {
        // 生成当前实例的唯一标识：IP + 随机数或进程号
        this.workerId = InetAddress.getLocalHost().getHostAddress() + "-" + ThreadLocalRandom.current().nextInt(1000);
    }

    /**
     * 发送消息
     */
    public void send(String topic, String payload) {
        String sql = "INSERT INTO sys_mq_task (topic, payload, execute_time) VALUES (?, ?, CURRENT_TIMESTAMP)";
        int update = jdbcTemplate.update(sql, topic, payload);
        log.info("update={}", update);
        log.info("update={}", update);
    }

    /**
     * 核心：分布式抢占并处理
     */
    public void pollAndConsume(String topic, Consumer<String> processor) {
        // 1. 扫描符合条件的待处理任务
        String selectSql = "SELECT id, topic, payload, version FROM sys_mq_task " +
                "WHERE topic = ? AND status = 0 AND execute_time <= CURRENT_TIMESTAMP " +
                "LIMIT 10";

        List<MqTask> tasks = jdbcTemplate.query(selectSql, new BeanPropertyRowMapper<>(MqTask.class), topic);

        for (MqTask task : tasks) {
            // 2. 核心原子抢占逻辑：只有更新成功的实例才能获得执行权
            // 通过 version 和 status=0 保证不会被其他实例重复领取
            String grabSql = "UPDATE sys_mq_task SET status = 1, worker_id = ?, version = version + 1, update_time = CURRENT_TIMESTAMP " +
                    "WHERE id = ? AND status = 0 AND version = ?";

            int rows = jdbcTemplate.update(grabSql, workerId, task.getId(), task.getVersion());

            if (rows > 0) {
                // 抢占成功，异步处理
                executor.submit(() -> {
                    try {
                        log.info("实例 [{}] 抢占任务成功: {}", workerId, task.getId());
                        processor.accept(task.getPayload());
                        // 处理成功：标记完成（物理删除或状态标记）
                        jdbcTemplate.update("DELETE FROM sys_mq_task WHERE id = ?", task.getId());
                    } catch (Exception e) {
                        log.error("任务执行失败，准备回滚或重试: {}", task.getId(), e);
                        handleFailure(task.getId());
                    }
                });
            }
        }
    }

    private void handleFailure(Long taskId) {
        // 失败逻辑：增加重试次数并放回队列，或者标记为失败
        String failSql = "UPDATE sys_mq_task SET status = 0, retry_count = retry_count + 1, worker_id = NULL " +
                "WHERE id = ? AND retry_count < max_retry";
        int rows = jdbcTemplate.update(failSql, taskId);
        if (rows == 0) {
            jdbcTemplate.update("UPDATE sys_mq_task SET status = 3 WHERE id = ?", taskId);
        }
    }

    /**
     * 故障恢复定时任务：重置掉那些领了任务但半天没干完的僵尸任务
     */
    public void recoverDeadTasks() {
        // 比如重置掉处理中超过 10 分钟的任务
        String sql = "UPDATE sys_mq_task SET status = 0, worker_id = NULL, version = version + 1 " +
                "WHERE status = 1 AND update_time < DATEADD('MINUTE', -10, CURRENT_TIMESTAMP)";
        int recovered = jdbcTemplate.update(sql);
        if (recovered > 0) log.warn("成功恢复了 {} 条僵死任务", recovered);
    }
}