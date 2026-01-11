package com.arc.nas.model.domain.app.media;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;


/**
 * 系统文件媒体库
 */
@TableName("media_file_resource")
public class MediaFileResource implements Serializable {

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

    public MediaFileResource() {

    }

    public MediaFileResource(Long id) {
        this.id = id;
    }

    public MediaFileResource(String path  ) {
        this.path = path;
    }
       public MediaFileResource(String path, Date updateTime  ) {
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

