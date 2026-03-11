package com.arc.nas.model.domain.system.common;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;


/**
 * 系统媒体库目录配置表
 */
@TableName("resource_config")
public class ResourceConfig implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Date createTime;// 创建时间
    private Date updateTime;// 更新时间
    private String remark;// 描述
    private String path;// 文件本地存放位置--本地存储的情况应该是服务器的绝对路径
    private String taskStatus;//标识系统对文件的处理流程
    private Long duration;//持续时间
    private Integer totalFileCount;
    private Integer totalFolderCount;
    private Long totalFileLength;

    public ResourceConfig() {

    }

    public ResourceConfig(Long id) {
        this.id = id;
    }

    public ResourceConfig(String path) {
        this.path = path;
    }

    public ResourceConfig(String path, Date updateTime) {
        this.path = path;
        this.updateTime = updateTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Integer getTotalFileCount() {
        return totalFileCount;
    }

    public void setTotalFileCount(Integer totalFileCount) {
        this.totalFileCount = totalFileCount;
    }

    public Integer getTotalFolderCount() {
        return totalFolderCount;
    }

    public void setTotalFolderCount(Integer totalFolderCount) {
        this.totalFolderCount = totalFolderCount;
    }

    public Long getTotalFileLength() {
        return totalFileLength;
    }

    public void setTotalFileLength(Long totalFileLength) {
        this.totalFileLength = totalFileLength;
    }
}

