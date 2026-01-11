package com.arc.nas.model.dto.app.media;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MediaItemDTO {
    private String code;            // 文件编号
    private Date createTime;// 创建时间
    private Date updateTime;// 更新时间
    @Deprecated
    private String originalName;    // 原始文件名
    private String displayName;     // 显示名称
    private String mediaType;       // VIDEO / IMAGE / AUDIO
    private String mimeType;        // MIME类型 video/mp4 / image/jpeg
    private String url;             // 可访问的播放或下载URL
    private Set<String> thumbnails;    // 缩略图URL，如果是视频可生成封面图
    private Long length;            // 文件大小，单位byte
    private String remark;          // 描述 / 文案
    private String path;
    private String taskStatus;      // 文件处理状态：PENDING / DONE / FAILED
    private Map<String, List<MediaSegmentDTO>> segmentMap;// 不通过分辨率的视频视频切片 original 4K 720 480 240

    public Map<String, List<MediaSegmentDTO>> getSegmentMap() {
        return segmentMap;
    }

    public void setSegmentMap(Map<String, List<MediaSegmentDTO>> segmentMap) {
        this.segmentMap = segmentMap;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Set<String> getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(Set<String> thumbnails) {
        this.thumbnails = thumbnails;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
