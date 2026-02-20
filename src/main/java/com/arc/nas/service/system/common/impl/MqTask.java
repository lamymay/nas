package com.arc.nas.service.system.common.impl;

import java.time.LocalDateTime;

public class MqTask {
    // --- 基础信息 ---
    private Long id;
    private String topic;      // 任务类型
    private String payload;    // JSON 内容

    // --- 控制逻辑 (核心) ---
    private Integer status;     // 0:就绪, 1:处理中, 3:死信(重试耗尽)
    private Integer version;    // 乐观锁版本号
    private Integer retryCount; // 当前重试次数
    private Integer maxRetry;   // 最大重试次数 (可针对不同任务设置不同上限)

    // --- 调度与跟踪 ---
    private String workerId;    // 抢占到任务的实例 ID
    private String lastError;   // 最后一次执行失败的原因描述
    private LocalDateTime executeTime; // 计划执行时间 (实现延迟/重试间隔)
    private LocalDateTime updateTime;  // 状态最后变更时间
    private LocalDateTime createTime;  // 任务创建时间

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Integer getMaxRetry() {
        return maxRetry;
    }

    public void setMaxRetry(Integer maxRetry) {
        this.maxRetry = maxRetry;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public LocalDateTime getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(LocalDateTime executeTime) {
        this.executeTime = executeTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
