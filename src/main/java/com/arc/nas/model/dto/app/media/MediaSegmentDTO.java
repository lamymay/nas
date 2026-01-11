package com.arc.nas.model.dto.app.media;

public class MediaSegmentDTO {

    private String url;        // 分段文件访问 URL
    private Long length;       // 分段大小（byte）
    private Integer index;     // 分段序号
    private Double duration;   // 分段时长（秒，可选）

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }
}
